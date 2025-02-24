/*
 * @Author: wanghao7717 792974788@qq.com
 * @Date: 2025-02-21 11:05:22
 * @LastEditors: wanghao7717 792974788@qq.com
 * @LastEditTime: 2025-02-24 15:36:50
 * 
 * 备注：采用该脚本生成API权限JSON，需注意以下几点：
 * 1. 页面中用到的API必须在server.js中定义
 * 2. 组件中目前识别通过import { *** } from '@/api/server'(.js可带可不带) 这种形式引入的API，其他形式的导入暂不支持。
 * 3. server.js中定义的API必须以export const开头，并且url不能通过变量方式传递，需要写在server.js中
 *    // export const saveBatchExecute = (url, data) => req.post(url, data)，这种方式传递就不行
 */

const fs = require('fs');
const path = require('path');
const compiler = require('vue-template-compiler');
const glob = require('glob');
const { v4: uuidv4 } = require('uuid');

// -------------------------------------------------------------------------------------------------------------------
// -------------------------------------------------给每个组件生成唯一key--------------------------------------------------------------
// -------------------------------------------------------------------------------------------------------------------

// 定义项目路径
const projectPath = path.resolve(__dirname, 'src'); // 假设 Vue 项目代码在 src 目录

// 匹配所有 .vue 文件
const vueFiles = glob.sync('**/*.vue', { cwd: projectPath });

// 为每个组件生成唯一 key 并写入文件
vueFiles.forEach((file) => {
  const filePath = path.join(projectPath, file);
  const content = fs.readFileSync(filePath, 'utf-8');
  const uniqueKey = uuidv4(); // 生成 UUID 作为唯一 key

  // 查找 <script> 标签的内容
  const scriptMatch = content.match(/<script[^>]*>[\s\S]*<\/script>/);
  if (!scriptMatch) {
    console.warn(`文件 ${file} 中未找到 <script> 标签，跳过处理。`);
    return;
  }

  const scriptContent = scriptMatch[0];

  // 检查是否已经存在 dataKey 属性
  const hasDataKey = scriptContent.includes('dataKey');
  if (hasDataKey) {
    // console.warn(`文件 ${file} 已经包含 dataKey 属性，跳过处理。`);
    return;
  }

  // 在 <script> 标签中插入 dataKey 属性
  const updatedScriptContent = scriptContent.replace(
    /export default {/,
    `export default {\n  dataKey: "${uniqueKey}",`
  );

  // 替换原文件中的 <script> 内容
  const updatedContent = content.replace(scriptContent, updatedScriptContent);

  // 保存修改后的内容
  fs.writeFileSync(filePath, updatedContent, 'utf-8');
  // console.log(`已为组件 ${file} 添加唯一 key: ${uniqueKey}`);
});


// -------------------------------------------------------------------------------------------------------------------
// -------------------------------------------------生成组件对应api的引用关系--------------------------------------------------------------
// -------------------------------------------------------------------------------------------------------------------


function extractApiImports(content) {
  // 匹配两种情况：'@/api/server' 和 '@/api/server.js'
  const importRegex = /import\s+\{([^}]+)\}\s+from\s+['"]@\/api\/server(\.js)?['"]/;
  const match = importRegex.exec(content);
  if (match) {
    const apiList = match[1].trim().split(',').map((item) => item.trim());
    return apiList;
  } else {
    return [];
  }
}

function scanFilesAndExtractImports(rootPath) {
  // 使用 glob 匹配所有 .vue 文件
  const files = glob.sync('**/*.vue', { cwd: rootPath });

  const dataKeyRegex = /dataKey:\s*["']([^"']+)["']/;
  const allImports = []; // 存储每个文件及其导入的 API 列表
  files.forEach((file) => {
    const content = fs.readFileSync(file, 'utf-8');
    // 查找 <script> 标签的内容
    const scriptMatch = content.match(/<script[^>]*>[\s\S]*<\/script>/);
    if (!scriptMatch) {
      console.warn(`文件 ${file} 中未找到 <script> 标签，跳过处理。`);
      return;
    }
    const scriptContent = scriptMatch[0];
    const keyMatch = scriptContent.match(dataKeyRegex);
    let dataKey = ''
    if (keyMatch) {
      dataKey = keyMatch[1]; // 提取 dataKey 的值
    } else {
      console.warn(`文件 ${file} 中未找到 dataKey 属性，跳过处理。`);
    }
    const apiImports = extractApiImports(content);
    if (apiImports.length > 0) {
      allImports[file] = apiImports;
      allImports.push({
        path: file,
        key: dataKey,
        api: apiImports
      })
    }
  });

  return allImports;
}

// 示例：扫描项目中的所有 .vue 文件
const rootPath_ = path.resolve(__dirname, '');
const allImports = scanFilesAndExtractImports(rootPath_);

// 第二个参数是replacer，第三个参数是空格数（用于缩进）
// const jsonString_ = JSON.stringify(allImports, null, 2);
// // 可选：将结果保存到文件
// fs.writeFileSync(path.join(__dirname, 'node-api-tree.json'), jsonString_, 'utf-8');



// ----------------------------------------------------------------------------------------
// --------------------------------------根据入口组件生成组件依赖data-key组成的数组-----------------
// ----------------------------------------------------------------------------------------

// 菜单对应组件入口集合
const menuPathMap = {
  "COLLABORATION_WORKFLOW_ORCHESTRATION": [
    "src/pages/collaboration/workflow-mgmt",
    "src/pages/collaboration/workflow"
  ],
  "COLLABORATION_PLUGIN_MANAGEMENT": [
    "src/pages/collaboration/plugin-registration-detail",
    "src/pages/collaboration/plugin-registration-list"
  ],
  "ADMIN_SYSTEM_PARAMS": [
    "src/pages/admin/system-params"
  ],
  "ADMIN_RESOURCES_MANAGEMENT": [
    "src/pages/admin/resources/index"
  ],
  "ADMIN_CERTIFICATION": [
    "src/pages/admin/plugin-certification"
  ],
  "ADMIN_USER_ROLE_MANAGEMENT": [
    "src/pages/admin/user-role-management"
  ],
  "ADMIN_SYSTEM_WORKFLOW_REPORT": [
    "src/pages/admin/workflow-report/index"
  ],
  "IMPLEMENTATION_WORKFLOW_EXECUTION": [
    "src/pages/implementation/workflow-execution/index",
    "src/pages/implementation/workflow-execution/normal-execution/template",
    "src/pages/implementation/workflow-execution/execution",
    "src/pages/implementation/workflow-execution/normal-execution/history",
    "src/pages/implementation/workflow-execution/time-execution/create",
    "src/pages/implementation/workflow-execution/time-execution/history",
    "src/pages/implementation/workflow-execution/execution"
  ],
  "IMPLEMENTATION_BATCH_EXECUTION": [
    "src/pages/implementation/batch-execution/index",
    "src/pages/implementation/batch-execution/execution/choose-template.vue",
    "src/pages/implementation/batch-execution/execution/create.vue",
    "src/pages/implementation/batch-execution/execution/list.vue",
    "src/pages/implementation/batch-execution/template/create.vue",
    "src/pages/implementation/batch-execution/template/list.vue"
  ],
  "ADMIN_BASE_MIGRATION": [
    "src/pages/admin/base-migration/index",
    "src/pages/admin/base-migration/export/create.vue",
    "src/pages/admin/base-migration/export/history.vue",
    "src/pages/admin/base-migration/import/create.vue",
    "src/pages/admin/base-migration/import/history.vue"
  ],
  "ADMIN_SYSTEM_DATA_MODEL": [
    "src/pages/admin/system-data-model"
  ]
}
const menuKeysMap = {}
Object.entries(menuPathMap).forEach(([menu, pathArr]) => {
  const keys = []; // 存储同一菜单下的所有keys
  pathArr.forEach(entry_path => {
    const projectRoot = path.resolve(__dirname, '');
    if (!entry_path.endsWith('.vue')) {
      entry_path = entry_path + '.vue'
    }
    const entryComponentPath = path.resolve(projectRoot, entry_path); // 根据实际情况调整

    // 存储已解析的组件路径，避免重复解析
    const parsedComponents = new Set();

    // 解析组件并提取 key 值
    function parseComponent(filePath) {
        if (parsedComponents.has(filePath)) return;
        parsedComponents.add(filePath);

        const content = fs.readFileSync(filePath, 'utf-8');
        const parsed = compiler.parseComponent(content);
        const scriptContent = parsed.script.content;

        // 提取 key 值（假设 key 是通过 script 中的变量定义的）
        const keyMatch = scriptContent.match(/dataKey:\s*['"]([^'"]+)['"]/);
        if (keyMatch) {
            keys.push(keyMatch[1]);
        }

        // 提取 import 语句中的子组件路径
        const importRegex = /import\s+\w+\s+from\s+['"]([^'"]+)['"]/g;
        let importMatch;
        while ((importMatch = importRegex.exec(scriptContent))) {
            const relativePath = importMatch[1].replace(/@/g, path.resolve(__dirname, 'src')); // 替换 @ 别名
            let absolutePath = path.resolve(path.dirname(filePath), relativePath);
            if (!absolutePath.endsWith('.vue')) {
              absolutePath = absolutePath + '.vue'
            }
            if (fs.existsSync(absolutePath)) {
              parseComponent(absolutePath);
            } else {
              console.warn(`未找到组件文件: ${absolutePath}`);
            }
        }
    }

    // 从入口组件开始解析
    parseComponent(entryComponentPath);
  })
  menuKeysMap[menu] = keys
})
// console.log('提取的 key 值数组:', menuKeysMap);


// -------------------------------------------------------------------------------------------------------------


// 得到菜单和对应api的集合
const apiMap = {};
allImports.forEach(item => {
  apiMap[item.key] = item.api;
});


// 遍历第一个JSON对象，将对应的API合并到一个数组中，并去重
for (const [key, values] of Object.entries(menuKeysMap)) {
  menuKeysMap[key] = Array.from(
    values.reduce((acc, uuid) => {
      if (apiMap[uuid]) {
        apiMap[uuid].forEach(api => acc.add(api)); // 使用Set去重
      }
      return acc;
    }, new Set()) // 初始值为一个Set
  );
}
const jsonString_ = JSON.stringify(menuKeysMap, null, 2);
fs.writeFileSync(path.join(__dirname, 'node-api-tree.json'), jsonString_, 'utf-8');
// console.log(menuKeysMap);


// -------------------------------------------------------将api函数名转换成method和url组成的对象----------------------------------------------------


// 读取代码文件
const filePath = path.join(__dirname, 'src/api/server.js'); // 假设代码保存在 api.js 文件中
const code = fs.readFileSync(filePath, 'utf-8');

const apiConfigArr = []
const getApiConfigArr = () => {
  // 将server.js文件切割成api组成的数组结构
  const exportRegex = /export\s+const\s+([\s\S]+?)(?=\nexport\s+const\s+|$)/g;
  const apiArray = [];
  let match;

  while ((match = exportRegex.exec(code)) !== null) {
    const apiDefinition = match[1].trim();
    apiArray.push(apiDefinition);
  }

  apiArray.forEach(apiStr => {
    // 使用正则表达式匹配 key、method 和 url
    const keyMatch = apiStr.match(/(\w+)\s*=/);
    const methodUrlMatch = apiStr.match(/req\.(\w+)\(['"`]([^'"`]+)['"`]/);
    if (keyMatch && methodUrlMatch) {
      const key = keyMatch[1]; // 函数名称
      const method = methodUrlMatch[1].toUpperCase(); // 请求方法名
      const url = methodUrlMatch[2].replace(/\${(.*?)}/g, "{$1}"); // 接口 URL
      apiConfigArr.push({
        key,
        url,
        method
      });
    }
  })
  // 将结果写入 JSON 文件
  const outputFilePath = path.join(__dirname, 'api_config.json');
  fs.writeFileSync(outputFilePath, JSON.stringify(apiConfigArr, null, 2));
  // console.log(`API 配置已生成并保存到 ${outputFilePath}`);
}
getApiConfigArr();





// --------------------------------------------------------生成最终结果--------------------------------------------------------


const newMenuKeysMap = {}
Object.keys(menuKeysMap).forEach(key => {
  newMenuKeysMap[key] = []
  menuKeysMap[key].forEach(apiName => {
    apiConfigArr.forEach(item => {
      if (item.key === apiName) {
        newMenuKeysMap[key].push(item)
      }
    })
  })
})

let finalResult = []
Object.keys(newMenuKeysMap).forEach(key => {
  const menuUrlsObj = {
    menu: key,
    urls: newMenuKeysMap[key]
  }
  finalResult.push(menuUrlsObj)
})

const menuApiMapPath = path.join(__dirname, 'menu-api-map.json');
fs.writeFileSync(menuApiMapPath, JSON.stringify(finalResult, null, 2));
