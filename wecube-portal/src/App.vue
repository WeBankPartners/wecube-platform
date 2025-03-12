<template>
  <div id="wecube_app">
    <transition name="fade" mode="out-in">
      <router-view class="pages"></router-view>
    </transition>
  </div>
</template>
<script>
export default {
  mounted() {
    // remove loading
    const boxLoading = document.getElementById('boxLoading')
    const boxTitle = document.getElementById('boxTitle')
    boxLoading.style.display = 'none'
    boxTitle.style.display = 'none'
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
</style>
