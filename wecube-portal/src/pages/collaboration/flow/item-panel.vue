<template>
  <div id="itemPanel" ref="itemPanel">
    <div class="tool-component">组件库</div>
    <div class="icon-tool">
      <Tooltip content="Right Center text" placement="right" :delay="1000">
        <div
          class="item-tool"
          draggable="true"
          node-type="start"
          data-label="开始"
          task-category=""
          data-shape="circle-node"
          fill="white"
          line-width="1"
        >
          <img src="./icon/start.svg" class="item-tool-icon" draggable="false" />
          <div class="item-tool-name">开始</div>
        </div>
      </Tooltip>
      <Tooltip content="Right Center text" placement="right" :delay="1000">
        <div
          class="item-tool"
          draggable="true"
          node-type="end"
          data-label="结束"
          task-category=""
          data-shape="circle-node"
          fill="white"
          line-width="1"
        >
          <img src="./icon/end.svg" class="item-tool-icon" draggable="false" />
          <div class="item-tool-name">结束</div>
        </div>
      </Tooltip>
      <Tooltip content="Right Center text" placement="right" :delay="1000">
        <div
          class="item-tool"
          draggable="true"
          node-type="abnormal"
          data-label="异常"
          task-category=""
          data-shape="circle-node"
          fill="white"
          line-width="1"
        >
          <img src="./icon/lightning.svg" class="item-tool-icon" draggable="false" />
          <div class="item-tool-name">异常</div>
        </div>
      </Tooltip>
      <Tooltip content="Right Center text" placement="right" :delay="1000">
        <div
          class="item-tool"
          draggable="true"
          node-type="decision"
          data-label="判断"
          task-category=""
          data-shape="diamond-node"
          fill="white"
          line-width="1"
        >
          <img src="./icon/decision.svg" class="item-tool-icon" draggable="false" />
          <div class="item-tool-name">判断</div>
        </div>
      </Tooltip>
      <Tooltip content="Right Center text" placement="right" :delay="1000">
        <div
          class="item-tool"
          draggable="true"
          node-type="converge"
          data-label="汇聚节点"
          task-category=""
          data-shape="rect-node"
          fill="white"
          line-width="1"
        >
          <img src="./icon/converge.svg" class="item-tool-icon" draggable="false" />
          <div class="item-tool-name">汇聚节点</div>
        </div>
      </Tooltip>
      <Tooltip content="Right Center text" placement="right" :delay="1000">
        <div
          class="item-tool"
          draggable="true"
          node-type="human"
          data-label="人工节点"
          task-category="SUTN"
          data-shape="rect-node"
          fill="white"
          line-width="1"
        >
          <img src="./icon/human.svg" class="item-tool-icon" draggable="false" />
          <div class="item-tool-name">人工节点</div>
        </div>
      </Tooltip>
      <Tooltip content="Right Center text" placement="right" :delay="1000">
        <div
          class="item-tool"
          draggable="true"
          node-type="automatic"
          data-label="自动节点"
          task-category="SSTN"
          data-shape="rect-node"
          fill="white"
          line-width="1"
        >
          <img src="./icon/automatic.svg" class="item-tool-icon" draggable="false" />
          <div class="item-tool-name">自动节点</div>
        </div>
      </Tooltip>
      <Tooltip content="Right Center text" placement="right" :delay="1000">
        <div
          class="item-tool"
          draggable="true"
          node-type="data"
          data-label="数据节点"
          task-category="SDTN"
          data-shape="rect-node"
          fill="white"
          line-width="1"
        >
          <img src="./icon/data.svg" class="item-tool-icon" draggable="false" />
          <div class="item-tool-name">数据节点</div>
        </div>
      </Tooltip>
      <Tooltip content="Right Center text" placement="right" :delay="1000">
        <div
          class="item-tool"
          draggable="true"
          node-type="fixedTime"
          data-label="固定时间"
          task-category=""
          data-shape="rect-node"
          fill="white"
          line-width="1"
        >
          <img src="./icon/fixed-time.svg" class="item-tool-icon" draggable="false" />
          <div class="item-tool-name">固定时间</div>
        </div>
      </Tooltip>
      <Tooltip content="Right Center text" placement="right" :delay="1000">
        <div
          class="item-tool"
          draggable="true"
          node-type="timeInterval"
          data-label="时间间隔"
          task-category=""
          data-shape="rect-node"
          fill="white"
          line-width="1"
        >
          <img src="./icon/time-interval.svg" class="item-tool-icon" draggable="false" />
          <div class="item-tool-name">时间间隔</div>
        </div>
      </Tooltip>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ItemPanel',
  data () {
    return {
      itemVisible: false
    }
  },
  mounted () {
    const icons = [...this.$refs.itemPanel.querySelectorAll('.item-tool')]

    icons.forEach(icon => {
      icon.addEventListener('dragstart', event => {
        const shape = icon.getAttribute('data-shape')
        const label = icon.getAttribute('data-label')
        const fill = icon.getAttribute('fill')
        const lineWidth = Number(icon.getAttribute('line-width'))
        const nodeType = icon.getAttribute('node-type')
        const taskCategory = icon.getAttribute('task-category')

        /* 设置拖拽传输数据 */
        event.dataTransfer.setData(
          'dragComponent',
          JSON.stringify({
            label,
            shape,
            fill,
            lineWidth,
            nodeType,
            taskCategory
          })
        )
      })
    })

    // 阻止默认动作
    document.addEventListener(
      'drop',
      e => {
        e.preventDefault()
      },
      false
    )
  }
}
</script>

<style lang="scss">
#itemPanel {
  position: absolute;
  top: 137px;
  left: 32px;
  bottom: 0;
  z-index: 10;
  width: 72px;
  height: 80%;
  background: #fff;
  text-align: center;
  // padding-top: 65px;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.1);

  &.hidden {
    transform: translate(-100%, 0);
  }

  .icon-tool {
    padding: 10px;
    text-align: center;
    .iconfont {
      display: block;
      width: 40px;
      height: 40px;
      line-height: 40px;
      font-size: 30px;
      cursor: move;
      border: 1px solid transparent;
      margin: 0 auto;

      &:hover {
        border-color: #ccc;
      }
    }
    .node {
      width: 32px;
      height: 32px;
      margin-bottom: 10px;
      cursor: move;
    }
    .circle-start {
      height: 50px;
      width: 50px;
      line-height: 50px;
      // border-radius: 50%;
      // border: 1px solid #ccc;
      // background: white;
    }
    .circle-end {
      @extend .circle-start;
      // border: 2px solid #ccc;
      // background: white;
    }

    .warning {
      height: 40px;
      line-height: 40px;
      border-left: 4px solid #e6a23c;
      background: #f8ecda;
    }
    .end {
      height: 40px;
      line-height: 40px;
      border-radius: 10px;
      background: #f9e3e2;
    }
  }
}

.item-tool {
  background-color: #f0f0f0;
  width: 52px;
  height: 52px;
  margin-bottom: 8px;
  border-radius: 4px;
  cursor: move;
  .item-tool-icon {
    width: 24px;
    height: 24px;
    margin: 6px 0 4px 0;
  }
  .item-tool-name {
    font-size: 11px;
  }
}
.tool-component {
  padding: 6px 0;
  border-bottom: 1px solid #e8eaec;
}
</style>
