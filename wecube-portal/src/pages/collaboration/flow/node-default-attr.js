import decisionIcon from './icon/x.svg'
import abnormalIcon from './icon/lightning.svg'
import timeIntervalIcon from './icon/time-interval.svg'
import fixedTimeIcon from './icon/fixed-time.svg'
import dataIcon from './icon/data.svg'
import automaticIcon from './icon/automatic.svg'
import humanIcon from './icon/human.svg'
import convergeIcon from './icon/+.svg'

const nodeDefaultAttr = {
  start: {
    logoIcon: {}
  },
  end: {
    logoIcon: {}
  },
  abnormal: {
    logoIcon: {
      show: true,
      x: -12,
      y: -12,
      img: abnormalIcon,
      width: 24,
      height: 24,
      offset: 0
    }
  },
  decision: {
    logoIcon: {
      show: true,
      x: -12,
      y: -12,
      img: decisionIcon,
      width: 24,
      height: 24,
      offset: 0
    }
  },
  converge: {
    logoIcon: {
      show: true,
      x: -12,
      y: -12,
      img: convergeIcon,
      width: 24,
      height: 24,
      offset: 0
    }
  },
  human: {
    logoIcon: {
      show: true,
      x: -12,
      y: -12,
      img: humanIcon,
      width: 24,
      height: 24,
      offset: 0
    }
  },
  automatic: {
    logoIcon: {
      show: true,
      x: -12,
      y: -12,
      img: automaticIcon,
      width: 24,
      height: 24,
      offset: 0
    }
  },
  data: {
    logoIcon: {
      show: true,
      x: -12,
      y: -12,
      img: dataIcon,
      width: 24,
      height: 24,
      offset: 0
    }
  },
  fixedTime: {
    logoIcon: {
      show: true,
      x: -12,
      y: -12,
      img: fixedTimeIcon,
      width: 24,
      height: 24,
      offset: 0
    }
  },
  timeInterval: {
    logoIcon: {
      show: true,
      x: -12,
      y: -12,
      img: timeIntervalIcon,
      width: 24,
      height: 24,
      offset: 0
    }
  }
}

export { nodeDefaultAttr }
