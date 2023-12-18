export default {
  nodes: [
    {
      id: '1', // 非必选
      data: {
        action: '初始化'
      },
      x: 500, // 该元素在画布中的位置
      y: 100,
      style: {
        // 节点样式
        fill: '#39495b',
        lineDash: [1, 2],
        shadowOffsetX: 0,
        shadowOffsetY: 2,
        shadowColor: '#666',
        shadowBlur: 10,
        width: 160,
        height: 70
      },
      label: 'new Vue()', // 节点上的文本
      // node 文本默认样式
      labelCfg: {
        style: {
          fontSize: 24,
          fill: '#fff',
          textAlign: 'center',
          textBaseline: 'middle',
          fontWeight: 'bold'
        }
      },
      // 当前节点的多状态样式
      nodeStateStyles: {
        'nodeState:default': {
          fill: '#39495b'
        },
        'nodeState:hover': {
          fill: '#ffbd17'
        },
        'nodeState:selected': {
          fill: '#f1ac00'
        }
      },
      // 自定义锚点数量和位置
      anchorPoints: [
        [0, 0],
        [0.5, 0],
        [0, 1],
        [0.5, 1],
        [1, 0],
        [1, 1]
      ]
    },
    {
      id: '2',
      type: 'circle-node',
      style: {
        r: 50,
        width: 230,
        height: 60,
        fill: '#65b586',
        lineWidth: 0
      },
      x: 500,
      y: 300,
      label: '初始化\n事件和生命周期',
      labelCfg: {
        style: {
          lineWidth: 2,
          fontSize: 18,
          stroke: '#ccc',
          fill: '#fff',
          textAlign: 'center'
        }
      }
    },
    {
      id: '3',
      type: 'rect-node',
      style: {
        fill: '#fff',
        stroke: '#c96164',
        lineWidth: 3,
        width: 180,
        height: 60
      },
      x: 250,
      y: 170,
      label: 'beforeCreate',
      labelCfg: {
        style: {
          fill: '#c96164',
          fontSize: 20,
          fontWeight: '700',
          width: 200,
          height: 60
        }
      },
      anchorHotsoptStyles: {
        r: 11,
        fill: 'green'
      },
      anchorPointStyles: {
        r: 4,
        fill: '#fff',
        stroke: '#1890FF',
        lineWidth: 2
      }
    },
    {
      id: '4',
      x: 500,
      y: 450,
      type: 'rect-node',
      label: '初始化\n注入 & 校验',
      // direction: 'down',
      style: {
        fill: '#65b586',
        size: [100, 160],
        lineWidth: 0
      },
      labelCfg: {
        style: {
          fontSize: 12,
          fill: '#fff',
          stroke: '#65b586',
          textAlign: 'left',
          x: -30,
          y: 0
        }
      }
      /* anchorPoints: [
        [1, 0],
        [0, 0],
        [0.5, 1],
      ], */
    },
    {
      id: '5',
      x: 250,
      y: 320,
      label: 'created',
      type: 'rect-node',
      style: {
        fill: '#fff',
        stroke: '#c96164',
        lineWidth: 3,
        width: 180,
        height: 60
      },
      labelCfg: {
        style: {
          fontSize: 20,
          fill: '#c96164'
        }
      },
      logoIcon: {
        // 是否显示 icon，值为 false 则不渲染 icon
        show: true,
        x: -80,
        y: -16,
        // icon 的地址，字符串类型
        img: 'https://gw.alipayobjects.com/zos/basement_prod/4f81893c-1806-4de4-aff3-9a6b266bc8a2.svg',
        width: 32,
        height: 32,
        // 用于调整图标的左右位置
        offset: 0
      }
    },
    {
      id: '6',
      x: 500,
      y: 600,
      type: 'diamond-node',
      label: '是否指定 "el" 选项?',
      style: {
        size: [160, 100],
        fill: '#f1b953',
        stroke: '#f1b953'
      },
      labelCfg: {
        style: {
          fontSize: 14,
          fill: '#fff',
          stroke: '#f1b953'
        }
      }
    },
    {
      id: '7',
      x: 750,
      y: 600,
      label: '当调用\n vm.$mount(el)\n 函数时',
      style: {
        fill: '#65b586',
        size: [160, 200],
        lineWidth: 0
      },
      labelCfg: {
        style: {
          fill: '#fff',
          stroke: '#65b586',
          fontSize: 12,
          textAlign: 'left',
          x: -40,
          y: -30
        }
      },
      type: 'triangle-node'
    },
    {
      x: 500,
      y: 800,
      id: '8',
      label: '是否指定 "template" 选项',
      labelCfg: {
        style: {
          fontSize: 12
        }
      },
      style: {
        rx: 100,
        ry: 30
      },
      type: 'ellipse-node'
    },
    {
      id: '9',
      x: 250,
      y: 800,
      label: '将 template 编译\n到 render 函数中',
      labelCfg: {
        style: {
          fontSize: 12
        }
      },
      style: {
        width: 160,
        height: 60
      }
    },
    {
      id: '10',
      x: 750,
      y: 800,
      label: '将 el 外部的 HTML\n作为 template 编译',
      labelCfg: {
        style: {
          fontSize: 12
        }
      },
      style: {
        width: 160,
        height: 60
      }
    },
    {
      id: '11',
      x: 500,
      y: 1000,
      label: '结束',
      labelCfg: {
        style: {
          fontSize: 16,
          fill: '#fff'
        }
      },
      style: {
        width: 160,
        height: 60,
        fill: '#fdbc33',
        lineWidth: 0
      }
    }
  ],
  edges: [
    {
      source: '1',
      target: '2'
    },
    {
      source: '1',
      target: '3'
    },
    {
      source: '2',
      target: '4'
    },
    {
      source: '2',
      target: '5'
    },
    {
      source: '4',
      target: '6'
    },
    {
      source: '6',
      target: '7'
    },
    {
      source: '6',
      target: '8'
    },
    {
      source: '8',
      target: '9'
    },
    {
      source: '8',
      target: '10'
    },
    {
      source: '9',
      target: '11'
    },
    {
      source: '10',
      target: '11'
    }
  ]
}
