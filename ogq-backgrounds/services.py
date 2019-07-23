class _VTTKonanService(object):
    VTT_SERVER = settings.VTT_TAGGING_ENDPOINT
    gv_dao = models.GoogleVisionResult.objects
    vtt_dao = models.VTTTagResult.objects

    def save(self, image_id, raw_json, google_tags):
        self.gv_dao.create(image_id=image_id, json_str=raw_json)
        self.vtt_dao.filter(image_id=image_id).update(google_tags=google_tags, completed=True)

    def tagging(self, raw_data):
        files = {'image': ("{}.{}".format(uuid.uuid1().hex, "jpg"), raw_data, "multipart/form-data")}
        response = requests.post(self.VTT_SERVER, files=files, timeout=1)
        response.raise_for_status()
        data = ast.literal_eval(response.content)
        return list(set([each['name'] for each in data if each.get('name')]))

    def list(self, last_id, limit):
        return self.vtt_dao.select_related('image').filter(Q(completed=True) & Q(id__gt=last_id)).order_by('id')[:limit]
