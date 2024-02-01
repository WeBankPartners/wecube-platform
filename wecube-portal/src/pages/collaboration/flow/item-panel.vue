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
        <div slot="content">
          <p>功能:事件节点1,标志编排开始</p>
          <p>使用规范:</p>
          <p>1.一个编排有且只有一个开始节点</p>
          <p>2.只有一条出线.</p>
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
        <div slot="content">
          <p>功能:事件节点2,标志编排结束</p>
          <p>使用规范:</p>
          <p>1.一个编排有且只有一个结束节点</p>
          <p>2.只有一条入线</p>
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
        <div slot="content">
          <p>功能:事件节点3,标志编排退出</p>
          <p>使用规范:</p>
          <p>1.一个编排可以有多个退出节点</p>
          <p>2.只有一条入线</p>
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
        <div slot="content">
          <p>功能:判断人工节点的填表结果,根据结果走到不同路线</p>
          <p>使用规范:</p>
          <p>1.判断节点只能接在人工任务节点后面</p>
          <p>2.支持一条入线两条出线</p>
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
        <div slot="content">
          <p>功能:并行线路的起点,并行线路都完成才能走到汇聚节点</p>
          <p>使用规范:</p>
          <p>1.分支节点和汇聚节点配套使用</p>
          <p>2.单进多出</p>
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
        <div slot="content">
          <p>功能:并行线路的终点,并行线路都完成才能走到汇聚节点</p>
          <p>使用规范:</p>
          <p>1.分支节点和汇聚节点配套使用</p>
          <p>2.多进单出</p>
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
        <div slot="content">
          <p>功能:任务节点1,在[任务-工作台]自动创建一个人工任务</p>
          <p>使用规范:</p>
          <p>1.发布编排之后,可以在[任务-模版管理-筛选本编排-编辑-任务表单设置]中,配置同名的任务表单</p>
          <p>2.如需要支持审批,请在后面接一个判断节点,任务表单会根据判断条件自动生成一个判断表单</p>
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
        <div slot="content">
          <p>功能:任务节点2,将自动执行插件服务API</p>
          <p>使用规范:</p>
          <p>1.操作目标对象: 设置[3.数据绑定-对象定位规则]</p>
          <p>2.操作使用API:选择[操作[协同-插件注册-某插件-服务注册-插件服务API],设置对应的入参,才能执行程序</p>
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
        <div slot="content">
          <p>功能:任务节点3,执行WECMDB的数据写入</p>
          <p>使用规范:</p>
          <p>1.配置数据写入的目标对象和操作类型,WECMDB将自动写入数据,支持设置多个目标对象</p>
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
        <div slot="content">
          <p>功能:时间节点1,控制流程进行下一步的具体时间</p>
          <p>使用规范:</p>
          <p>1.支持设置未来的一个时刻,格式为年月日时分秒</p>
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
          <div class="item-tool-name">{{ $t('timeInterval') }}</div>
        </div>
        <div slot="content">
          <p>功能:时间节点2,控制流程进行下一步的间隔</p>
          <p>使用规范:</p>
          <p>1.支持选择一个时间段</p>
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
  top: 133px;
  left: 22px;
  bottom: 0;
  z-index: 10;
  width: 90px;
  overflow: auto;
  height: calc(100vh - 240px);
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
