<template>
  <div id="wecube_app">
    <transition name="fade" mode="out-in">
      <router-view class="pages"></router-view>
    </transition>
    <div class="floating-robot" @click="openChatRobot" title="wecube答疑机器人">
      <img src="@/assets/robot.png" alt="Robot" />
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {}
  },
  mounted() {
    // remove loading
    const boxLoading = document.getElementById('boxLoading')
    const boxTitle = document.getElementById('boxTitle')
    boxLoading.style.display = 'none'
    boxTitle.style.display = 'none'
  },
  methods: {
    openChatRobot() {
      window.open('***REMOVED***', '_blank')
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
  position: fixed;
  width: 48px;
  height: 48px;
  border-radius: 60px;
  cursor: pointer;
  z-index: 9999;
  user-select: none;
  right: 20px;
  bottom: 20px;
  transition: transform 0.1s ease;
  background: transparent;
  pointer-events: auto;
  
  &:hover {
    transform: scale(1.1);
  }
  
  img {
    width: 100%;
    height: 100%;
    border-radius: 48px;
    object-fit: contain;
    background: transparent;
    mix-blend-mode: multiply;
  }
}
</style>
