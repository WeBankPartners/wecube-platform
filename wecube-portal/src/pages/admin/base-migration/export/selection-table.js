export default {
  data() {
    return {
      // 角色表
      roleTableLoading: false,
      roleTableColumns: [
        {
          type: 'selection',
          width: 55,
          align: 'center'
        },
        {
          title: '业务产品名',
          minWidth: 180,
          key: 'name',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.jumpToHistory(params.row)
              }}
            >
              {params.row.name}
            </span>
          )
        },
        {
          title: '产品ID',
          minWidth: 180,
          key: 'id'
        },
        {
          title: '产品描述',
          key: 'description',
          minWidth: 140
        },
        {
          title: this.$t('updatedBy'),
          key: 'updatedBy',
          minWidth: 120
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 150
        }
      ],
      roleTableData: [],
      roleSelectionList: [],
      // 编排表
      flowTableLoading: false,
      flowTableColumns: [
        {
          type: 'selection',
          width: 55,
          align: 'center'
        },
        {
          title: '业务产品名',
          minWidth: 180,
          key: 'name',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.jumpToHistory(params.row)
              }}
            >
              {params.row.name}
            </span>
          )
        },
        {
          title: '产品ID',
          minWidth: 180,
          key: 'id'
        },
        {
          title: '产品描述',
          key: 'description',
          minWidth: 140
        },
        {
          title: this.$t('updatedBy'),
          key: 'updatedBy',
          minWidth: 120
        },
        {
          title: this.$t('table_updated_date'),
          key: 'updatedTime',
          minWidth: 150
        }
      ],
      flowTableData: [],
      flowSelectionList: [],
      flowSearchOptions: [
        {
          key: 'name',
          placeholder: '业务产品名',
          component: 'input'
        },
        {
          key: 'id',
          placeholder: '产品ID',
          component: 'input'
        }
      ],
      flowSearchParams: {
        name: '',
        id: ''
      }
    }
  }
}
