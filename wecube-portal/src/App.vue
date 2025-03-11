<template>
  <div id="wecube_app">
    <transition name="fade" mode="out-in">
      <router-view v-if="noAuthPages.includes($route.path)" class="pages"></router-view>
      <Layout v-else />
    </transition>
  </div>
</template>
<script>
import Layout from "@/pages/index"
export default {
  components: { Layout },
  data() {
    return {
      noAuthPages: ['/login', '/404']
    }
  },
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
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #0f1222; // Fes字体颜色规范
  height: 100%;
  min-width: 1280px;
}
</style>