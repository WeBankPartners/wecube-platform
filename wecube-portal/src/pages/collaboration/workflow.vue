<template>
  <div class="root">
    <div>
      <Button type="success" class="btn-right" @click="create">
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
import { flowMgmt } from '@/api/server.js'
export default {
  data () {
    return {}
  },
  methods: {
    async create () {
      const params = {
        id: '',
        name: '编排2401111105',
        version: '1',
        scene: '',
        authPlugins: [],
        tags: '',
        conflictCheck: false,
        rootEntity: '',
        permissionToRole: {
          MGMT: ['SUPER_ADMIN', 'CMDB_ADMIN'],
          USE: ['SUPER_ADMIN']
        }
      }
      const { data, status } = await flowMgmt(params)
      if (status === 'OK') {
        console.log(11, data)
      }
    }
  }
}
</script>

<style lang="scss">
.btn-right {
  margin-right: 10px;
}
</style>
