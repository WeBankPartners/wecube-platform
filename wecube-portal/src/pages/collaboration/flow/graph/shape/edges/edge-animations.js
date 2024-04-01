/**
 * 边动画合集
 */

const ball = {
  run (group) {
    // 获得当前边的第1个图形，这里是边本身的 path
    const path = group.get('children')[0]
    const endArrowShape = path.get('endArrowShape')
    const arrowSize = endArrowShape
      ? endArrowShape.get('bbox')
        ? Math.max(endArrowShape.get('bbox').width, endArrowShape.get('bbox').height)
        : 0
      : 0
    const startPoint = path.getPoint(0)
    const length = path.getTotalLength()
    const num = Math.floor(length / 100) || 1

    if (length <= 40) return // 线段太短就不要动画了

    for (let i = 0; i < num; i++) {
      const timeout = setTimeout(() => {
        const circle = group.addShape('circle', {
          attrs: {
            x: startPoint.x,
            y: startPoint.y,
            fill: '#1890ff',
            r: 2
          },
          className: 'edge-runner',
          name: 'edge-runner'
        })

        circle.animate(
          ratio => {
            const tmpPoint = path.getPoint(ratio)
            const opacity = length - length * ratio >= arrowSize ? 1 : 0

            // ! 必须设置这个属性为false, 否则当起始位置落在画布外时将无法播放动画
            circle.set('hasChanged', false)

            // 返回需要变化的参数集，这里返回了位置 x 和 y
            return {
              ...tmpPoint,
              opacity
            }
          },
          {
            duration: length >= 100 ? length * 3 : length * 5,
            repeat: true
          }
        )
      }, i * length)

      group.runners.push(timeout)
    }
  },
  stop (group) {
    const runners = []

    group.get('children').forEach(child => {
      if (child.get('className') === 'edge-runner') {
        child.stopAnimate()
        runners.push(child)
      }
    })

    runners.forEach(runner => runner.remove())
    // 清除所有定时器
    group.runners.forEach(settimeout => {
      clearTimeout(settimeout)
    })
    group.running = false
  }
}

const dash = {
  run (group) {
    let index = 0
    // 获得当前边的第1个图形，这里是边本身的 path
    const path = group.get('children')[0]
    const dashLine = group.addShape('path', {
      attrs: {
        offset: path.attrs.offset,
        path: path.attrs.path,
        stroke: '#fff',
        startArrow: false,
        endArrow: false
      },
      name: 'edge-dash'
    })

    dashLine.animate(
      radio => {
        index++
        if (index > 9) {
          index = 0
        }
        return {
          lineDash: [2, 1, 2, 4],
          lineDashOffset: -index
        }
      },
      {
        repeat: true,
        duration: 3000
      }
    )
  },
  stop (group) {
    // 获得当前边的第1个图形，这里是边本身的 path
    const path = group.get('children').find(item => item.cfg.name === 'edge-dash')

    if (path) {
      path.remove()
      group.running = false
    }
  }
}

export default {
  ball,
  dash
}
