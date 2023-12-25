import startIcon from './logo/start.svg'
import endIcon from './logo/end.svg'
import decisionIcon from './logo/x.svg'
import abnormalIcon from './logo/lightning.svg'
import timeIntervalIcon from './logo/time-interval.svg'
import fixedTimeIcon from './logo/fixed-time.svg'
import dataIcon from './logo/data.svg'
import automaticIcon from './logo/automatic.svg'
import humanIcon from './logo/human.svg'
import convergeIcon from './logo/+.svg'

const nodeDefaultAttr = {
  start: {
    logoIcon: {
      show: true,
      x: -12,
      y: -12,
      img: startIcon,
      width: 24,
      height: 24,
      offset: 0
    },
    anchorPoints: [
      // [0.5, 0],
      // [0, 0.5],
      // [0.5, 1],
      [1, 0.5]
    ]
  },
  end: {
    logoIcon: {
      show: true,
      x: -12,
      y: -12,
      img: endIcon,
      width: 24,
      height: 24,
      offset: 0
    },
    anchorPoints: [
      // [0.5, 0],
      [0, 0.5]
      // [0.5, 1],
      // [1, 0.5]
    ]
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
    },
    anchorPoints: [
      // [0.5, 0],
      [0, 0.5]
      // [0.5, 1],
      // [1, 0.5]
    ]
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
    },
    anchorPoints: [
      // [0.5, 0],
      [0, 0.5],
      // [0.5, 1],
      [1, 0.5]
    ]
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
    },
    anchorPoints: [
      // [0.5, 0],
      [0, 0.5],
      // [0.5, 1],
      [1, 0.5]
    ]
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
    },
    anchorPoints: [
      // [0.5, 0],
      [0, 0.5],
      // [0.5, 1],
      [1, 0.5]
    ]
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
    },
    anchorPoints: [
      // [0.5, 0],
      [0, 0.5],
      // [0.5, 1],
      [1, 0.5]
    ]
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
    },
    anchorPoints: [
      // [0.5, 0],
      [0, 0.5],
      // [0.5, 1],
      [1, 0.5]
    ]
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
    },
    anchorPoints: [
      // [0.5, 0],
      [0, 0.5],
      // [0.5, 1],
      [1, 0.5]
    ]
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
    },
    anchorPoints: [
      // [0.5, 0],
      [0, 0.5],
      // [0.5, 1],
      [1, 0.5]
    ]
  }
}

export { nodeDefaultAttr }
