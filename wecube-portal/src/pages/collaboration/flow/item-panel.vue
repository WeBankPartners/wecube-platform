<template>
  <div id="itemPanel" ref="itemPanel">
    <div class="tool-component">{{ $t('components') }}</div>
    <div class="icon-tool">
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="start"
          :data-label="$t('start')"
          data-shape="circle-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <img
            src="./icon/start.svg"
            style="border: 1px solid #303030; border-radius: 50%"
            class="item-tool-icon"
            draggable="false"
          />
          <div class="item-tool-name">{{ $t('start') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('startNodeFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('startNodetip1') }}</p>
          <p>{{ $t('startNodetip2') }}</p>
        </div>
      </Tooltip>
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="end"
          :data-label="$t('end')"
          data-shape="circle-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <img
            src="./icon/end.svg"
            style="border: 1px solid #303030; border-radius: 50%"
            class="item-tool-icon"
            draggable="false"
          />
          <div class="item-tool-name">{{ $t('end') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('endNodeFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('endNodetip1') }}</p>
          <p>{{ $t('endNodetip2') }}</p>
        </div>
      </Tooltip>
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="abnormal"
          :data-label="$t('abnormal')"
          data-shape="circle-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <img
            src="./icon/abnormal.svg"
            style="border: 1px solid #303030; border-radius: 50%"
            class="item-tool-icon"
            draggable="false"
          />
          <div class="item-tool-name">{{ $t('abnormal') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('abnormalNodeFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('abnormalNodetip1') }}</p>
          <p>{{ $t('abnormalNodetip2') }}</p>
        </div>
      </Tooltip>
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="decision"
          :data-label="$t('decision')"
          data-shape="diamond-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <div class="diamond-border">
            <img src="./icon/descision-panel.svg" class="item-tool-icon" draggable="false" />
          </div>
          <div class="diamond-item-tool-name">{{ $t('decision') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('decisionNodeFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('decisionNodetip1') }}</p>
          <p>{{ $t('decisionNodetip2') }}</p>
        </div>
      </Tooltip>
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="fork"
          :data-label="$t('forkNode')"
          data-shape="diamond-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <div class="diamond-border">
            <img src="./icon/fork-panel.svg" class="item-tool-icon" draggable="false" />
          </div>
          <div class="diamond-item-tool-name">{{ $t('forkNode') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('forkNodeFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('forkNodetip1') }}</p>
          <p>{{ $t('forkNodetip2') }}</p>
        </div>
      </Tooltip>
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="merge"
          :data-label="$t('merge')"
          data-shape="diamond-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <div class="diamond-border">
            <img src="./icon/merge-panel.svg" class="item-tool-icon" draggable="false" />
          </div>
          <div class="diamond-item-tool-name">{{ $t('merge') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('forkNodeFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('forkNodetip1') }}</p>
          <p>{{ $t('forkNodetip2') }}</p>
        </div>
      </Tooltip>
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="human"
          :data-label="$t('artificial')"
          data-shape="rect-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <img
            src="./icon/human.svg"
            style="border: 1px solid #303030; width: 37px; height: 28px"
            class="item-tool-icon"
            draggable="false"
          />
          <div class="item-tool-name">{{ $t('artificial') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('humanNodeFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('humanNodetip1') }}</p>
          <p>{{ $t('humanNodetip2') }}</p>
        </div>
      </Tooltip>
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="automatic"
          :data-label="$t('automatic')"
          data-shape="rect-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <img
            src="./icon/automatic.svg"
            style="border: 1px solid #303030; width: 37px; height: 28px"
            class="item-tool-icon"
            draggable="false"
          />
          <div class="item-tool-name">{{ $t('automatic') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('automaticNodeFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('automaticNodetip1') }}</p>
          <p>{{ $t('automaticNodetip2') }}</p>
        </div>
      </Tooltip>
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="data"
          :data-label="$t('data')"
          data-shape="rect-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <img
            src="./icon/data.svg"
            style="border: 1px solid #303030; width: 32px"
            class="item-tool-icon"
            draggable="false"
          />
          <div class="item-tool-name">{{ $t('data') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('dataNodeFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('dataNodetip1') }}</p>
        </div>
      </Tooltip>
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="date"
          :data-label="$t('fixedTime')"
          data-shape="circle-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <img
            src="./icon/date.svg"
            style="border: 1px solid #303030; border-radius: 50%"
            class="item-tool-icon"
            draggable="false"
          />
          <div class="item-tool-name">{{ $t('fixedTime') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('fixedTimeFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('fixedTimeNodetip1') }}</p>
        </div>
      </Tooltip>
      <Tooltip :maxWidth="400" placement="right" :delay="1000">
        <div
          class="item-tool"
          :draggable="editFlow"
          node-type="timeInterval"
          :data-label="$t('timeInterval')"
          data-shape="circle-node"
          fill="white"
          line-width="1"
          stroke="#303030"
        >
          <img
            src="./icon/timeInterval.svg"
            style="border: 1px solid #303030; border-radius: 50%"
            class="item-tool-icon"
            draggable="false"
          />
          <div class="item-tool-name">{{ $t('timeGap') }}</div>
        </div>
        <div style="word-break: keep-all" slot="content">
          <p>{{ $t('timeIntervalFunc') }}</p>
          <p>{{ $t('usage') }}</p>
          <p>{{ $t('timeIntervaltip1') }}</p>
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
      itemVisible: false,
      editFlow: true
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
        const stroke = icon.getAttribute('stroke')

        /* 设置拖拽传输数据 */
        event.dataTransfer.setData(
          'dragComponent',
          JSON.stringify({
            label,
            shape,
            fill,
            lineWidth,
            stroke,
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
  },
  methods: {
    setEditFlowStatus (editFlow) {
      this.editFlow = editFlow
    }
  }
}
</script>
<style lang="scss">
.ivu-tooltip-inner-with-width {
  white-space: normal !important;
}
</style>
<style lang="scss" scoped>
#itemPanel {
  position: absolute;
  top: 134px;
  left: 22px;
  bottom: 0;
  z-index: 10;
  width: 90px;
  overflow: auto;
  height: calc(100vh - 154px);
  text-align: center;
  // padding-top: 65px;
  transition: transform 0.3s ease-in-out;
  box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.1);
  background: white;

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
    margin-top: 8px;
  }
  .item-tool-name {
    font-size: 11px;
  }
}
.tool-component {
  padding: 6px 0;
  border-bottom: 1px solid #e8eaec;
}

.diamond-border {
  position: relative;
  width: 44px; /* 调整为你的图片尺寸 */
  height: 44px; /* 调整为你的图片尺寸 */
  overflow: hidden;
  padding: 4px;
  bottom: 6px;
}

.diamond-border img {
  display: block;
  width: 100%;
  height: auto;
  transform: rotate(45deg);
  transform-origin: 50% 50%;
  position: absolute;
  top: 16%;
  left: 80%;
  margin-top: -50%;
  margin-left: -50%;
  border: 1px solid #303030;
  padding: 3px;
}
.diamond-item-tool-name {
  position: relative;
  bottom: 8px;
  font-size: 11px;
}
</style>
