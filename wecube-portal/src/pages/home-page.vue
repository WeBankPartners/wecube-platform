<template>
  <div>
    <p style="margin-top: 20px; height: 200px; font-size: 24px !important;">
      {{ $t("wecube_desc") }}
    </p>
    <div class="graph_container">
      <div class="scene">
        <div class="cube rotating">
          <div class="cube-face front"></div>
          <div class="cube-face inner-faces inner-face"></div>
          <div class="cube-face inner-faces inner-face2"></div>
          <div class="cube-face inner-faces inner-face3"></div>
          <div class="cube-face back"></div>
          <div class="cube-face left"></div>
          <div class="cube-face right"></div>
          <div class="cube-face top"></div>
          <div class="cube-face bottom"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentLanguage: "",
      toggle: true
    };
  },
  async created() {
    this.getLocalLang();
  },
  methods: {
    getLocalLang() {
      this.currentLanguage = localStorage.getItem("lang") || navigator.language;
    }
  }
};
</script>

<style lang="scss" scoped>
.graph_container {
  position: absolute;
  text-align: center;
  height: calc(100vh - 200px);
  width: calc(100vw - 550px);
  left: 300px;
  background-image: url("./images/wecube-bg.png");
  background-repeat: no-repeat;
  background-size: contain;
}
.scene {
  position: relative;
  margin: 150px auto;
  width: 200px;
  height: 200px;
}
.cube {
  width: 100%;
  height: 100%;
  position: relative;
  transform-style: preserve-3d;
  transition: 2s;
}
.rotating {
  animation: spinner 10s infinite linear;
}

.cube-face {
  width: inherit;
  height: inherit;
  position: absolute;
  transition: 2s;
  box-shadow: 0px 0px 130px #59f;
  background-repeat: no-repeat;
  background-color: #111;
  opacity: 0.7;
  line-height: 200px;
  color: #fff;
  text-align: center;
  font-family: sans-serif;
  font-size: 20px;
  font-weight: 800;
}

.front {
  transform: translate3d(0, 0, 100px);
  transition: 2s;
}
.back {
  transform: rotateY(180deg) translate3d(0, 0, 100px);
}
.left {
  transform: rotateY(-90deg) translate3d(0, 0, 100px);
}
.right {
  transform: rotateY(90deg) translate3d(0, 0, 100px);
}
.top {
  transform: rotateX(90deg) translate3d(0, 0, 100px);
}
.bottom {
  transform: rotateX(-90deg) translate3d(0, 0, 100px);
}

@keyframes spinner {
  from {
    transform: rotateY(0deg) rotateX(0deg);
  }
  to {
    transform: rotateY(359deg) rotateX(359deg);
  }
}
.inner-faces {
  background: url("./images/core.gif");
  background-position: center;
  background-size: cover;
  // opacity:.6;
  box-shadow: 0px 0px 0px #fff;
}

.inner-face {
  transform: translate3d(0, 0, 0px);
}
.inner-face2 {
  transform: rotateY(90deg) translate3d(0, 0, 0px);
}
.inner-face3 {
  transform: rotateX(90deg) translate3d(0, 0, 0px);
}
</style>
