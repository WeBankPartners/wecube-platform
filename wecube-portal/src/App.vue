<template>
  <div id="wecube_app">
    <transition name="fade" mode="out-in">
      <router-view class="pages"></router-view>
    </transition>
    <div
      class="floating-robot"
      @click="handleClick"
      :title="$t('platform_robot_title') + '\n' + $t('platform_robot_content')"
      @mousedown="handleMouseDown"
      @mouseup="handleMouseUp"
      @mousemove="handleMouseMove"
      :style="{ top: `${dragObj.top}px`, left: `${dragObj.left}px` }"
    >
      <img src="@/assets/robot.png" alt="Robot" />
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      dragObj: {
        top: window.innerHeight / 2, // 窗口高度减去图标高度和边距
        left: window.innerWidth - 68, // 窗口宽度减去图标宽度和边距
        isDragging: false, // 是否正在拖动
        startX: 0, // 鼠标按下时的初始 X 坐标
        startY: 0, // 鼠标按下时的初始 Y 坐标
        startTop: 0, // 鼠标按下时元素的初始顶部位置
        startLeft: 0, // 鼠标按下时元素的初始左侧位置
        pressTimer: null,
        isLongPress: false,
        hasMoved: false  // 添加标记，用于判断是否发生了移动
      }
    }
  },
  mounted() {
    // remove loading
    const boxLoading = document.getElementById('boxLoading')
    const boxTitle = document.getElementById('boxTitle')
    boxLoading.style.display = 'none'
    boxTitle.style.display = 'none'

    // 监听窗口大小变化，保持图标在右下角
    window.addEventListener('resize', this.updatePosition)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updatePosition)
  },
  methods: {
    updatePosition() {
      if (!this.dragObj.isDragging) {
        this.dragObj.top = window.innerHeight - 68
        this.dragObj.left = window.innerWidth - 68
      }
    },
    openChatRobot() {
      window.open('https://aigc.weoa.com/s/chatdoc/#/bots/BOTefe7ae80f972470086b2a0edfa47e18a/share', '_blank')
    },
    handleMouseDown(event) {
      // 阻止默认事件和冒泡
      event.preventDefault();
      event.stopPropagation();
      
      this.dragObj.hasMoved = false;  // 重置移动标记
      this.dragObj.pressTimer = setTimeout(() => {
        this.dragObj.isLongPress = true;
        this.startDrag(event);
      }, 200); // 200ms长按触发
    },
    handleMouseUp(event) {
      // 阻止默认事件和冒泡
      event.preventDefault();
      event.stopPropagation();
      
      clearTimeout(this.dragObj.pressTimer);
      if (this.dragObj.isLongPress) {
        this.stopDrag();
        this.dragObj.isLongPress = false;
      }
    },
    handleMouseMove(event) {
      if (this.dragObj.isLongPress && this.dragObj.isDragging) {
        // 阻止默认事件和冒泡
        event.preventDefault();
        event.stopPropagation();
        
        this.dragObj.hasMoved = true;  // 标记发生了移动
        this.onDragging(event);
      }
    },
    handleClick(event) {
      // 如果发生了拖动，则不触发点击事件
      if (this.dragObj.hasMoved) {
        event.preventDefault();
        event.stopPropagation();
        return;
      }
      this.openChatRobot();
    },
    startDrag(event) {
      // 开始拖动
      this.dragObj.isDragging = true;
      this.dragObj.startX = event.clientX;
      this.dragObj.startY = event.clientY;
      this.dragObj.startTop = this.dragObj.top;
      this.dragObj.startLeft = this.dragObj.left;

      // 监听鼠标移动和释放事件
      document.addEventListener("mousemove", this.onDragging);
      document.addEventListener("mouseup", this.stopDrag);
    },
    onDragging(event) {
      // 拖动中
      if (this.dragObj.isDragging) {
        // 计算新的位置
        let newTop = this.dragObj.startTop + event.clientY - this.dragObj.startY
        let newLeft = this.dragObj.startLeft + event.clientX - this.dragObj.startX

        // 限制上下边界
        const minTop = 10
        const maxTop = window.innerHeight - 58 // 图标高度48px + 边界10px
        newTop = Math.max(minTop, Math.min(newTop, maxTop))

        // 限制左右边界
        const minLeft = 10
        const maxLeft = window.innerWidth - 58 // 图标宽度48px + 边界10px
        newLeft = Math.max(minLeft, Math.min(newLeft, maxLeft))

        this.dragObj.top = newTop
        this.dragObj.left = newLeft
      }
    },
    stopDrag() {
      // 停止拖动
      this.dragObj.isDragging = false;
      document.removeEventListener("mousemove", this.onDragging);
      document.removeEventListener("mouseup", this.stopDrag);
    }
  }
}
</script>

<style lang="scss" scoped>
#wecube_app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #0f1222; // Fes字体颜色规范
  height: 100%;
  min-width: 1280px;
}

.floating-robot {
  position: absolute;
  width: 48px;
  height: 48px;
  border-radius: 60px;
  cursor: pointer;
  z-index: 9999;
  // right: 20px;
  // bottom: 20px;
  transition: transform 0.1s ease;
  background: transparent;
  user-select: none; /* 防止选中文本 */

  &:hover {
    transform: scale(1.1);
  }
  
  img {
    width: 100%;
    height: 100%;
    border-radius: 48px;
    object-fit: contain;
    background: transparent;
  }
}
</style>
