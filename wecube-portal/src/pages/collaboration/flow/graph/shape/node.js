/**
 * @param {string} shapeType 支持 rect/circel/path 等内置节点
 * @param {string} getShapeStyle 用于覆盖 base-node 默认样式
 * @param {string} icon 图片 url
 * @param {string} labelCfg 文本节点样式
 */
import defaultStyles from './defaultStyles'

const { iconStyles, nodeStyles, anchorPointStyles, nodeLabelStyles } = defaultStyles

function getStyle (options, cfg) {
  let xx = {
    ...cfg,
    // 自定义默认样式
    ...nodeStyles,
    ...options,
    // 当前节点样式
    ...cfg.style,
    // 文本配置
    labelCfg: {
      ...nodeLabelStyles,
      ...cfg.labelCfg,
      style: {
        ...nodeLabelStyles.style,
        ...(cfg.labelCfg ? cfg.labelCfg.style : {})
      }
    },
    // 图标样式
    iconStyles: {
      ...iconStyles,
      ...cfg.iconStyles
    },
    // 锚点样式
    anchorPointStyles: {
      ...anchorPointStyles,
      ...cfg.anchorPointStyles
    },
    ...cfg.nodeStateStyles,
    // 锚点高亮样式
    anchorHotsoptStyles: cfg.anchorHotsoptStyles
  }
  return xx
}

export default G6 => {
  // 从 base-node 中扩展方形节点
  G6.registerNode(
    'rect-node',
    {
      shapeType: 'rect',
      // 当前节点的样式集合
      getShapeStyle (cfg) {
        const width = cfg.style.width || 80
        const height = cfg.style.height || 40

        return getStyle.call(
          this,
          {
            width,
            height,
            radius: 5,
            // 将图形中心坐标移动到图形中心, 用于方便鼠标位置计算
            x: -width / 2,
            y: -height / 2
          },
          cfg
        )
      }
    },
    'base-node'
  )

  // 扩展圆形节点
  G6.registerNode(
    'circle-node',
    {
      shapeType: 'circle',
      getShapeStyle (cfg) {
        const r = cfg.style.r || 24
        const xx = getStyle.call(
          this,
          {
            r, // 半径
            // 将图形中心坐标移动到图形中心, 用于方便鼠标位置计算
            x: 0,
            y: 0
          },
          cfg
        )
        return xx
      }
    },
    'base-node'
  )

  // 扩展椭圆形
  G6.registerNode(
    'ellipse-node',
    {
      shapeType: 'ellipse',
      getShapeStyle (cfg) {
        return getStyle.call(
          this,
          {
            rx: 50,
            ry: 30,
            // 将图形中心坐标移动到图形中心, 用于方便鼠标位置计算
            x: 0,
            y: 0
          },
          cfg
        )
      }
    },
    'base-node'
  )

  // 扩展模态节点
  G6.registerNode(
    'modelRect-node',
    {
      shapeType: 'rect',
      getShapeStyle (cfg) {
        const width = cfg.style.width || 200
        const height = cfg.style.height || 80

        return getStyle.call(
          this,
          {
            width,
            height,
            radius: 5,
            // 将图形中心坐标移动到图形中心, 用于方便鼠标位置计算
            x: -width / 2,
            y: -height / 2
          },
          cfg
        )
      }
    },
    'base-node'
  )

  // 扩展菱形
  G6.registerNode(
    'diamond-node',
    {
      shapeType: 'path', // 非内置 shape 要指定为path
      getShapeStyle (cfg) {
        const path = this.getPath(cfg)

        return getStyle.call(
          this,
          {
            path,
            x: 0,
            y: 0
          },
          cfg
        )
      },
      // 返回菱形的路径
      getPath (cfg) {
        const size = cfg.style.size || [50, 50] // 如果没有 size 时的默认大小
        const width = size[0]
        const height = size[1]

        //  / 1 \
        // 4     2
        //  \ 3 /
        return [
          ['M', 0, -height / 2], // 上部顶点
          ['L', width / 2, 0], // 右侧顶点
          ['L', 0, height / 2], // 下部顶点
          ['L', -width / 2, 0], // 左侧顶点
          ['Z'] // 封闭
        ]
      }
    },
    'base-node'
  )

  // 扩展三角形节点
  G6.registerNode(
    'triangle-node',
    {
      shapeType: 'path',
      getShapeStyle (cfg) {
        const path = this.getPath(cfg)

        return getStyle.call(
          this,
          {
            direction: cfg.direction || 'up',
            path,
            x: 0,
            y: 0
          },
          cfg
        )
      },
      getPath (cfg) {
        const direction = cfg.direction || 'up'
        const size = cfg.style.size || [60, 100] // 如果没有 size 时的默认大小
        const width = size[0]
        const height = size[1]

        const path = [
          // ['M', 0, -height / 2], // 上部顶点
          // ['L', -width / 2, 0], // 左侧顶点
          // ['L', width / 2, 0], // 右侧顶点
          ['Z'] // 封闭
        ]

        if (direction === 'up') {
          path.unshift(
            ['M', 0, -height / 2],
            ['L', -width / 2, 0], // 左侧顶点
            ['L', width / 2, 0] // 右侧顶点
          )
        } else {
          path.unshift(
            ['M', 0, height / 2],
            ['L', -width / 2, 0], // 左侧顶点
            ['L', width / 2, 0] // 右侧顶点
          )
        }

        return path
      },
      getAnchorPoints (cfg) {
        return (
          cfg.anchorPoints || [
            [0.5, 0],
            [0, 1],
            [1, 1]
          ]
        )
      }
    },
    'base-node'
  )
}
