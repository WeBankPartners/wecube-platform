export default {
  data() {
    return {
      // cmdb数据
      cmdbCIColumns: [
        {
          title: 'CI名称',
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
          title: '总数',
          key: 'total',
          width: 90,
          render: (h, params) => (
            <span style="display:flex;align-items:center;justify-content:space-between;">
              {params.row.count}
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
      // 物料包数据
      artifactsColumns: [
        {
          title: '底座产品',
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
          title: '总包数',
          key: 'total',
          width: 80,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              {params.row.count}
              <Icon
                type="ios-list"
                size="36"
                style="cursor:pointer;"
                onClick={() => {
                  this.handleDetai(params.row, 'artifacts')
                }}
              />
            </span>
          )
        }
      ],
      // 监控数据
      monitorColumns: [
        {
          title: '数据类型',
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
          title: '已选',
          key: 'total',
          width: 80,
          render: (h, params) => (
            <span style="display:flex;align-items:center;">
              {params.row.count}
              <Icon
                type="ios-list"
                size="36"
                style="cursor:pointer;"
                onClick={() => {
                  this.handleDetai(params.row, 'monitor')
                }}
              />
            </span>
          )
        },
        {
          title: '监控配置查询条件',
          key: 'conditions',
          render: (h, params) => <span>{params.row.conditions || '-'}</span>
        }
      ]
    }
  }
}
