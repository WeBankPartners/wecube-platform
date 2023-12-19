<template>
  <div id="itemPanel" ref="itemPanel">
    <div class="icon-tool">
      <i
        class="node circle-start"
        draggable="true"
        node-type="start"
        data-label="开始"
        data-shape="circle-node"
        fill="white"
        line-width="1"
        >开始</i
      >
      <i
        class="node circle-end"
        draggable="true"
        node-type="end"
        data-label="结束"
        data-shape="circle-node"
        fill="white"
        line-width="3"
        >结束</i
      >
      <i
        class="node circle-end"
        draggable="true"
        node-type="abnormal"
        data-label="异常"
        data-shape="circle-node"
        fill="white"
        line-width="1"
        >异常</i
      >
      <!-- <i class="node warning" draggable="true" data-label="警告" data-shape="rect-node" fill="#f8ecda">警告</i>
      <i class="node end" draggable="true" data-label="结束" data-shape="rect-node" fill="#f9e3e2">结束</i> -->
      <img
        src="./icon/decision.svg"
        style="width: 50px; height: 50px"
        draggable="true"
        node-type="decision"
        data-label="判断"
        data-shape="diamond-node"
        class="node iconfont icon-diamond"
        alt=""
      />
      <img
        src="./icon/converge.svg"
        style="width: 50px; height: 50px"
        draggable="true"
        node-type="converge"
        data-label="汇聚节点"
        data-shape="rect-node"
        class="node iconfont icon-rect"
        alt=""
      />
      <img
        src="./icon/human.svg"
        style="width: 50px; height: 50px"
        draggable="true"
        node-type="human"
        data-label="人工节点"
        data-shape="rect-node"
        class="node iconfont icon-rect"
        alt=""
      />
      <img
        src="./icon/automatic.svg"
        style="width: 50px; height: 50px"
        draggable="true"
        node-type="automatic"
        data-label="自动节点"
        data-shape="rect-node"
        class="node iconfont icon-rect"
        alt=""
      />
      <img
        src="./icon/data.svg"
        style="width: 50px; height: 50px"
        draggable="true"
        node-type="data"
        data-label="数据节点"
        data-shape="rect-node"
        class="node iconfont icon-rect"
        alt=""
      />
      <i
        class="node circle-end"
        draggable="true"
        node-type="fixedTime"
        data-label="固定时间"
        data-shape="circle-node"
        fill="white"
        line-width="1"
        >固定</i
      >
      <i
        class="node circle-end"
        draggable="true"
        node-type="timeInterval"
        data-label="时间间隔"
        data-shape="circle-node"
        fill="white"
        line-width="1"
        >间隔</i
      >

      <!-- <i draggable="true" data-label="圆形节点" data-shape="circle-node" class="node iconfont icon-circle" /> -->
      <!-- <i draggable="true" data-label="方形节点" data-shape="rect-node" class="node iconfont icon-rect" /> -->
      <!-- <i
        draggable="true"
        data-label="测试节点"
        data-shape="rect-node"
        style="border: 1px solid red"
        class="node iconfont icon-rect"
      />
      <i draggable="true" data-label="椭圆形节点" data-shape="ellipse-node" class="node iconfont icon-ellipse" />
      <i draggable="true" data-label="菱形节点" data-shape="diamond-node" class="node iconfont icon-diamond" />
      <i draggable="true" data-label="对话框节点" data-shape="modelRect-node" class="node iconfont icon-model-rect" />
      <i class="split" />
      <i draggable="true" class="gb-toggle-btn" @click="itemVisible = !itemVisible" /> -->
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
    const icons = [...this.$refs.itemPanel.querySelector('.icon-tool').querySelectorAll('.node')]

    icons.forEach(icon => {
      icon.addEventListener('dragstart', event => {
        const shape = icon.getAttribute('data-shape')
        const label = icon.getAttribute('data-label')
        const fill = icon.getAttribute('fill')
        const lineWidth = Number(icon.getAttribute('line-width'))
        const nodeType = icon.getAttribute('node-type')
        /* 设置拖拽传输数据 */
        event.dataTransfer.setData(
          'dragComponent',
          JSON.stringify({
            label,
            shape,
            fill,
            lineWidth,
            nodeType
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
  top: 145px;
  left: 32px;
  bottom: 0;
  z-index: 10;
  width: 100px;
  height: 80%;
  background: #fff;
  // padding-top: 65px;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.1);

  &.hidden {
    transform: translate(-100%, 0);
  }

  .gb-toggle-btn {
    width: 10px;
    height: 20px;
    top: 50%;
    left: 100%;
    border-radius: 0 10px 10px 0;
    box-shadow: 2px 0 2px 0 rgba(0, 0, 0, 0.1);
    transform: translate(0, -50%);
  }

  .split {
    height: 1px;
    display: block;
    background: #e0e0e0;
    margin: 5px 0;
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
      display: block;
      margin-bottom: 10px;
      cursor: move;
    }
    .circle-start {
      height: 50px;
      width: 50px;
      line-height: 50px;
      border-radius: 50%;
      border: 1px solid #ccc;
      background: white;
    }
    .circle-end {
      @extend .circle-start;
      border: 2px solid #ccc;
      background: white;
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
</style>
