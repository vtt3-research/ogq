package com.ogqcorp.bgh.fragment;

import java.util.Map;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.billingclient.api.Purchase;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.pie.PieInfoFragment;
import com.ogqcorp.bgh.pie.inappbilling.GoogleBillingStore;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.AdFreeItem;
import com.ogqcorp.bgh.spirit.data.AdFreeItemData;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.commons.PreventDoubleTap;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.ToastUtils;

public class PurchaseAdFreeDialogFragment extends DialogFragment
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	public PurchaseAdFreeDialogFragment()
	{
		// Nothing
	}

	public static DialogFragment start(FragmentManager fragmentManager)
	{
		DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT);
		if (fragment == null)
		{
			fragment = new PurchaseAdFreeDialogFragment();
			//fragment.show(fragmentManager, TAG_FRAGMENT);

			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.add(fragment, TAG_FRAGMENT);
			ft.commitAllowingStateLoss();
		}

		return fragment;
	}

	public static DialogFragment start(FragmentManager fragmentManager, String actionPrefix)
	{
		DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT);
		if (fragment == null)
		{
			Bundle args = new Bundle();
			args.putString(KEY_ACTION_PREFIX, actionPrefix);

			fragment = new PurchaseAdFreeDialogFragment();
			fragment.setArguments(args);
			//fragment.show(fragmentManager, TAG_FRAGMENT);

			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.add(fragment, TAG_FRAGMENT);
			ft.commitAllowingStateLoss();
		}

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public int getTheme()
	{
		return R.style.BG_Theme_Dialog_Search;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.dialog_purchase_adfree, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_unbinder = ButterKnife.bind(this, view);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			getDialog().getWindow().setStatusBarColor(0x66000000);
		}

		if (getArguments() != null && getArguments().containsKey(KEY_ACTION_PREFIX))
		{
			m_actionPrefix = getArguments().getString(KEY_ACTION_PREFIX);
		}

		m_loadingDialog = new MaterialDialog.Builder(getActivity())
				.content(R.string.processing)
				.progress(true, 0)
				.cancelable(false)
				.show();

		// Google 결제 서비스 연결 체크
		GoogleBillingStore.getInstance().init(getContext(), new GoogleBillingStore.BillingServiceCallback()
		{
			@Override
			public void onSuccess(int code)
			{
				if (FragmentUtils.isDestroyed(PurchaseAdFreeDialogFragment.this) == true) return;

				loadAdFreeItems();
			}

			@Override
			public void onFail()
			{
				if (FragmentUtils.isDestroyed(PurchaseAdFreeDialogFragment.this) == true) return;

				dismissByError();
			}
		}, new GoogleBillingStore.BillingPurchaseCallback()
		{
			@Override
			public void onSuccess()
			{
				if (FragmentUtils.isDestroyed(PurchaseAdFreeDialogFragment.this) == true) return;

				ToastUtils.makeInfoToast(getContext(), Toast.LENGTH_LONG, R.string.adfree_purchase_success).show();
				m_loadingDialog.dismiss();
				dismiss();
			}

			@Override
			public void onFail()
			{
				dismissByError();
			}
		});
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		return new Dialog(getActivity(), getTheme())
		{
			@Override
			public void onBackPressed()
			{
				PurchaseAdFreeDialogFragment.this.dismiss();
			}
		};
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();

		if (m_loadingDialog != null)
		{
			m_loadingDialog.dismiss();
		}
		GoogleBillingStore.getInstance().release();
	}

	@Override
	public void show(FragmentManager manager, String tag)
	{
		try
		{
			FragmentTransaction ft = manager.beginTransaction();
			ft.add(this, tag);
			ft.commitAllowingStateLoss();
		}
		catch (IllegalStateException e)
		{
			//Nothing
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog)
	{
		super.onDismiss(dialog);
	}

	@OnClick({ R.id.close, R.id.outside })
	public void onClose()
	{
		String action = "Close_Nativead";
		if (TextUtils.isEmpty(m_actionPrefix) == false)
		{
			action = "Close_" + m_actionPrefix;
		}

		AnalyticsManager.getInstance().adFreeEvent(getActivity(), action);
		dismiss();
	}

	@OnClick(R.id.buy)
	public void onClickBuy()
	{
		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == false)
			return;

		String action = "Confirm_Nativead";
		if (TextUtils.isEmpty(m_actionPrefix) == false)
		{
			action = "Confirm_" + m_actionPrefix;
		}

		AnalyticsManager.getInstance().adFreeEvent(getActivity(), action);

		GoogleBillingStore.getInstance().purchase(getActivity(), m_adFreeItem.getProductId(), new GoogleBillingStore.BillingCallback()
		{
			@Override
			public void onSuccess(int callbackType, Map<String, String> priceMap, Purchase purchase, String productId, String token)
			{
				if (FragmentUtils.isDestroyed(PurchaseAdFreeDialogFragment.this) == true) return;
			}

			@Override
			public void onFail(int errorcode, String productId)
			{
				if (FragmentUtils.isDestroyed(PurchaseAdFreeDialogFragment.this) == true) return;

				ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
			}
		});
	}

	//=========================================================================
	// public Method
	//=========================================================================

	public void loadAdFreeItems()
	{
		Requests.authRequestByGet(UrlFactory.pieADFreeItems(), AdFreeItemData.class, new Response.Listener<AdFreeItemData>()
		{
			@Override
			public void onResponse(AdFreeItemData response)
			{
				try
				{
					m_adFreeItem = response.getAdFreeItem();
					if (m_adFreeItem.getPurchased() == true)
					{
						m_loadingDialog.dismiss();
						dismiss();
						Fragment fragment = PieInfoFragment.newInstance();
						AbsMainActivity.getTabStackHelper(PurchaseAdFreeDialogFragment.this).showFragment(fragment);
					}
					else
					{
						loadProduct();
					}

				}
				catch (Exception e)
				{
					dismissByError();
				}
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				dismissByError();
			}
		});
	}

	private void loadProduct()
	{
		final String pid = m_adFreeItem.getProductId();

		// Google 결제 아이템 조회
		GoogleBillingStore.getInstance().queryPurchasesItem(pid, new GoogleBillingStore.BillingCallback()
		{
			@Override
			public void onSuccess(int callbackType, Map<String, String> priceMap, Purchase purchase, String productId, String token)
			{
				if (FragmentUtils.isDestroyed(PurchaseAdFreeDialogFragment.this) == true) return;

				m_loadingDialog.dismiss();

				if (callbackType == GoogleBillingStore.BILLING_CALLBACK_TYPE_QUERYITEM)
				{
					if (priceMap != null)
					{
						m_price = priceMap.get(pid);
						priceMap.clear();

						StringBuffer buffer = new StringBuffer().append(m_price).append(" / ").append(getString(R.string.adfree_dialog_term));
						m_buyBtn.setText(buffer.toString());
						m_container.setVisibility(View.VISIBLE);

						/*GoogleBillingStore.getInstance().setConsume(true);
						GoogleBillingStore.getInstance().queryAllConsume(new GoogleBillingStore.BillingCallback()
						{
							@Override
							public void onSuccess(int callbackType, Map<String, String> priceMap, Purchase purchase, String productId, String token)
							{
								if (FragmentUtils.isDestroyed(PurchaseAdFreeDialogFragment.this) == true) return;

								StringBuffer buffer = new StringBuffer().append(m_price).append(" / ").append(getString(R.string.adfree_dialog_term));
								m_buyBtn.setText(buffer.toString());
								m_container.setVisibility(View.VISIBLE);
							}

							@Override
							public void onFail(int errorcode, String productId)
							{
								if (FragmentUtils.isDestroyed(PurchaseAdFreeDialogFragment.this) == true) return;

								ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
								m_loadingDialog.dismiss();
								dismiss();
							}
						});*/
					}
				}
			}

			@Override
			public void onFail(int errorcode, String productId)
			{
				if (FragmentUtils.isDestroyed(PurchaseAdFreeDialogFragment.this) == true) return;

				dismissByError();
			}
		});
	}

	private void dismissByError()
	{
		try
		{
			if(getContext() != null)
				ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
			m_loadingDialog.dismiss();
			dismiss();
		} catch (Exception e)
		{
		}
	}

	//=========================================================================
	// Constants
	//=========================================================================
	private static final String KEY_ACTION_PREFIX = "KEY_ACTION_PREFIX";
	private static final String TAG_FRAGMENT = "PurchaseAdFreeDialogFragment";

	//=========================================================================
	// Variables
	//=========================================================================

	private Unbinder m_unbinder;

	private String m_actionPrefix;
	private String m_price;
	private AdFreeItem m_adFreeItem;
	private MaterialDialog m_loadingDialog;

	@BindView(R.id.description1) TextView m_description1;
	@BindView(R.id.description2) TextView m_description2;
	@BindView(R.id.container) ViewGroup m_container;
	@BindView(R.id.buy) Button m_buyBtn;
}