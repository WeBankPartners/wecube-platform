<template>
  <div>
    <p style="margin-top: 20px; height: 200px; font-size: 24px !important">
      {{ $t('wecube_desc') }}
    </p>
    <div class="graph_container">
      <div class="wrap">
        <div class="cube">
          <div class="front">
            <h2>{{ $t('cube_front') }}</h2>
            Workbench 汇聚任务 统一平台
          </div>
          <div class="back">
            <h2>{{ $t('cube_back') }}</h2>
            Expectation 规范设计 描绘期望
          </div>
          <div class="top">
            <h2>{{ $t('cube_top') }}</h2>
            Equilibration 不断调整 保持对等
          </div>
          <div class="bottom">
            <h2>{{ $t('cube_bottom') }}</h2>
            Wisdom 赋予智慧 制定策略
          </div>
          <div class="left">
            <h2>{{ $t('cube_left') }}</h2>
            Execution 自动执行 驱动实现
          </div>
          <div class="right">
            <h2>{{ $t('cube_right') }}</h2>
            Watching 持续监测 发现差异
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentLanguage: '',
      toggle: true
    }
  },
  async created() {
    this.getLocalLang()
  },
  methods: {
    getLocalLang() {
      this.currentLanguage = localStorage.getItem('lang') || navigator.language
    }
  }
}
</script>

<style lang="scss" scoped>
.wrap {
  margin-top: 100px;
}

@keyframes bganim {
  from {
    background-position: 0px;
  }

  to {
    background-position: 80px;
  }
}

.cube {
  margin: auto;
  position: relative;
  height: 200px;
  width: 200px;
  transform-style: preserve-3d;
}

.cube div {
  position: absolute;
  box-sizing: border-box;
  height: 100%;
  width: 100%;
  opacity: 0.9;
  background-color: #111;
  border: solid 1px #eee;
  box-shadow: 0px 0px 130px #59f;
  color: #fff;
  transition: transform 0.2s ease-in;
  text-align: center;
  padding: 60px 0;
}

.front {
  transform: translateZ(100px);
}

.back {
  transform: translateZ(-100px) rotateY(180deg);
}

.right {
  transform: rotateY(-270deg) translateX(100px);
  transform-origin: top right;
}

.left {
  transform: rotateY(270deg) translateX(-100px);
  transform-origin: center left;
}

.top {
  transform: rotateX(-270deg) translateY(-100px);
  transform-origin: top center;
}

.bottom {
  transform: rotateX(270deg) translateY(100px);
  transform-origin: bottom center;
}

@keyframes rotate {
  from {
    transform: rotateX(0deg) rotateY(0deg);
  }

  to {
    transform: rotateX(360deg) rotateY(360deg);
  }
}

.cube {
  animation: rotate 20s infinite linear;
}

.wrap:hover .front {
  transform: translateZ(200px);
}

.wrap:hover .back {
  transform: translateZ(-200px) rotateY(180deg);
}

.wrap:hover .right {
  transform: rotateY(-270deg) translateZ(100px) translateX(100px);
}

.wrap:hover .left {
  transform: rotateY(270deg) translateZ(100px) translateX(-100px);
}

.wrap:hover .top {
  transform: rotateX(-270deg) translateZ(100px) translateY(-100px);
}

.wrap:hover .bottom {
  transform: rotateX(270deg) translateZ(100px) translateY(100px);
}
</style>
