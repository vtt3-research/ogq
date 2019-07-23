class BackgroundVTTLabelAnnotationAPIView(_APIView):
    permission_classes = (IsAuthenticated,)
    vtt_konan_service = services.vtt_konan_service

    def post(self, request):
        if not request.user.author.has_upload_permission():
            return JsonResponse({'error': 'permission denied'}, status=403)
        try:
            serializer = post_serializers.LabelAnnotationSerializer(data=request.data, context={'request': request})
            if serializer.is_valid():
                exists = image_service.is_exists_image(serializer.validated_data['image'])

                return JsonResponse({
                    'data': [],
                    'exists': exists
                })

            return JsonResponse(serializer.errors, status=400)
        except Exception as ex:
            logger.error('error to call vtt tagging: {}'.format(ex), exc_info=True)
            return JsonResponse({'data': []})


class VTTView(_APIView):
    vtt_konan_service = services.vtt_konan_service
    page_size = 100

    @method_decorator(gzip_page)
    def get(self, request):

        serializer = post_serializers.VTTResultReqSerializer(data=request.query_params)
        if not serializer.is_valid():
            return JsonResponse({'error': serializer.errors}, status=400)

        data = serializer.validated_data
        key = data['key']

        if not key or not models.PartnerApi.objects.filter(uuid=key).exists():
            return JsonResponse({'error': 'Invalidate api key'}, status=401)

        last_id = data['last_id']

        vtt_tag_results = self.vtt_konan_service.list(last_id, self.page_size)

        serializer = serializers.VttResSerializer(vtt_tag_results, many=True)

        return JsonResponse({'data': serializer.data})