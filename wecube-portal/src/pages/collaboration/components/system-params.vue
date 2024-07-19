<template>
  <div>
    <Table :columns="tableColumns" :data="tableData"></Table>
  </div>
</template>

<script>
import { getSysParams } from '@/api/server'
export default {
  name: 'sys-params',
  data () {
    return {
      tableData: [],
      tableColumns: [
        {
          title: this.$t('source'),
          key: 'scope'
        },
        {
          title: this.$t('name'),
          key: 'name'
        },
        {
          title: this.$t('table_value'),
          key: 'value',
          render: (h, params) => {
            return h('span', params.row.value || params.row.defaultValue)
          }
        },
        {
          title: this.$t('status'),
          key: 'status'
        }
      ]
    }
  },
  watch: {
    pkgId: {
      handler: function (val) {
        val && this.getData(val)
      }
    }
  },
  props: {
    pkgId: {
      required: true
    }
  },
  mounted () {
    this.getData(this.pkgId)
  },
  methods: {
    async getData (pkgId) {
      let { status, data } = await getSysParams(pkgId)
      if (status === 'OK') {
        this.tableData = data
      }
    }
  }
}
</script>
