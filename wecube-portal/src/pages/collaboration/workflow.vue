<template>
  <div class="root">
    <div>
      <Button type="success" class="btn-right">
        <Icon type="ios-add-circle-outline" size="16"></Icon>
        {{ $t('create') }}
      </Button>
      <Button type="primary" class="btn-right">
        <Icon type="ios-cloud-upload-outline" size="16"></Icon>
        {{ $t('import_flow') }}
      </Button>
      <Button type="info" class="btn-right">
        <Icon type="ios-cloud-download-outline" size="16"></Icon>
        {{ $t('export_flow') }}
      </Button>
      <Button type="warning" class="btn-right">
        <Icon type="ios-person-outline" size="16"></Icon>
        {{ $t('config_permission') }}
      </Button>
      <Button type="error" class="btn-right">
        <Icon type="ios-trash-outline" size="16"></Icon>
        {{ $t('delete') }}
      </Button>
    </div>
  </div>
</template>

<script>
export default {
  data () {
    return {}
  }
}
</script>

<style lang="scss">
.btn-right {
  margin-right: 10px;
}
</style>
