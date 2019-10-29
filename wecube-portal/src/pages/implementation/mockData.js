export const modelData = [
  {
    id: "UNIT_APP",
    srcGraphNodeIds: [],
    toGraphNodeIds: ["INS1", "INS2", "PACK_V1"],
    refFlowNodeIds: []
  },
  {
    id: "INS1",
    srcGraphNodeIds: ["UNIT_APP"],
    toGraphNodeIds: ["HOST1", "PACK_V1"],
    refFlowNodeIds: []
  },
  {
    id: "HOST1",
    srcGraphNodeIds: ["INS1"],
    toGraphNodeIds: ["DISK1", "IP1"],
    refFlowNodeIds: []
  },
  {
    id: "DISK1",
    srcGraphNodeIds: ["HOST1"],
    toGraphNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "IP1",
    srcGraphNodeIds: ["HOST1"],
    toGraphNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "PACK_V1",
    srcGraphNodeIds: ["INS1", "UNIT_APP", "INS2"],
    toGraphNodeIds: ["USER"],
    refFlowNodeIds: []
  },
  {
    id: "USER",
    srcGraphNodeIds: ["PACK_V1"],
    toGraphNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "INS2",
    srcGraphNodeIds: ["UNIT_APP"],
    toGraphNodeIds: ["HOST2", "PACK_V1"],
    refFlowNodeIds: []
  },
  {
    id: "HOST2",
    srcGraphNodeIds: ["INS2"],
    toGraphNodeIds: ["IP2", "DISK2"],
    refFlowNodeIds: []
  },
  {
    id: "IP2",
    srcGraphNodeIds: ["HOST2"],
    toGraphNodeIds: [],
    refFlowNodeIds: []
  },
  {
    id: "DISK2",
    srcGraphNodeIds: ["HOST2"],
    toGraphNodeIds: [],
    refFlowNodeIds: []
  }
];

export const flowData = [
  {
    id: 1,
    name: "开始",
    toGraphNodeIds: [2],
    refGraphNodeIds: []
  },
  {
    id: 2,
    name: "创建虚拟机",
    toGraphNodeIds: [3],
    refGraphNodeIds: ["HOST1", "HOST2"]
  },
  {
    id: 3,
    name: "创建挂载磁盘",
    toGraphNodeIds: [4],
    refGraphNodeIds: ["DISK1", "DISK2"]
  },
  {
    id: 4,
    name: "安装 saltstack agent",
    toGraphNodeIds: [5],
    refGraphNodeIds: ["INS1", "INS2"]
  },
  {
    id: 5,
    name: "安装监控agent",
    toGraphNodeIds: [6],
    refGraphNodeIds: ["INS1", "INS2"]
  },
  {
    id: 6,
    name: "创建部署用户",
    toGraphNodeIds: [7],
    refGraphNodeIds: []
  },
  {
    id: 7,
    name: "差异化变量替换",
    toGraphNodeIds: [8],
    refGraphNodeIds: []
  },
  {
    id: 8,
    name: "物料包分发解压",
    toGraphNodeIds: [9],
    refGraphNodeIds: []
  },
  {
    id: 9,
    name: "执行部署脚本",
    toGraphNodeIds: [10],
    refGraphNodeIds: []
  },
  {
    id: 10,
    name: "执行启动脚本",
    toGraphNodeIds: [11],
    refGraphNodeIds: []
  },
  {
    id: 11,
    name: "启动应用监控",
    toGraphNodeIds: [12],
    refGraphNodeIds: []
  },
  {
    id: 12,
    name: "结束",
    toGraphNodeIds: [],
    refGraphNodeIds: []
  }
];
