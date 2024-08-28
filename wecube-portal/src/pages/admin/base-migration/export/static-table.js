export default {
  data() {
    return {
      cmdbTableColumns: [
        {
          title: 'CI层级',
          key: 'level',
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
          title: 'CI名称',
          key: 'name'
        },
        {
          title: '总数',
          key: 'total',
          width: 80,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              {params.row.total}
              <Icon
                type="ios-list"
                size="36"
                style="cursor:pointer;"
                onClick={() => {
                  this.handleDetai(params.row, 'cmdb')
                }}
              />
            </span>
          )
        }
      ],
      cmdbTableData: []
    }
  }
}
