<template>
  <div class="migration-product-tree">
    <BaseSearch
      :onlyShowReset="true"
      :options="searchOptions"
      v-model="searchParams"
      @search="handleFilterProductData"
    ></BaseSearch>
    <Tree
      :render="renderTreeContent"
      ref="tree"
      :data="currentTreeData"
      show-checkbox
      @on-check-change="handleProductSelect"
    ></Tree>
  </div>
</template>

<script>
/**
 * 产品树形选择组件
 * 支持产品数据的树形展示、搜索过滤、多选等功能
 */
export default {
  props: {
    // 产品树形数据
    data: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      // 搜索参数
      searchParams: {
        displayName: ''
      },
      // 搜索配置选项
      searchOptions: [
        {
          key: 'displayName',
          placeholder: this.$t('pe_business_product'),
          component: 'input',
          width: '500px'
        }
      ],
      // 已选中的产品列表
      selectionList: [],
      // 原始产品数据
      productData: [],
      // 过滤后的产品数据
      filterProductData: [],
      // 当前树形展示的数据
      currentTreeData: []
    }
  },
  watch: {
    /**
     * 监听产品数据变化，更新当前树形数据
     */
    data: {
      handler(val) {
        this.productData = val
        this.updateCurrentTreeData()
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    /**
     * 更新当前树形数据
     * 根据是否有搜索关键词决定使用原始数据还是过滤后的数据
     */
    updateCurrentTreeData() {
      if (this.searchParams.displayName) {
        this.currentTreeData = this.filterProductData
      } else {
        this.currentTreeData = this.productData
      }
    },
    /**
     * 处理产品数据搜索过滤
     * 支持按产品名称搜索，并自动展开匹配的节点及其父节点
     */
    handleFilterProductData() {
      // 清空搜索词时，默认折叠所有节点
      if (!this.searchParams.displayName) {
        const expandAllData = (data, flag) => {
          data.forEach(x => {
            x.expand = false
            x.matched = false
            if (x.children && x.children.length > 0) {
              expandAllData(x.children, flag)
            }
          })
        }
        expandAllData(this.productData, false)
        this.updateCurrentTreeData()
        return
      }
      // 搜索关键字命中时，给节点打标记
      const matchData = (data) => {
        data.forEach(item => {
          const nameFlag = item.title.toLowerCase().indexOf(this.searchParams.displayName.toLowerCase()) > -1
          if (nameFlag) {
            this.$set(item, 'expand', true)
            this.$set(item, 'matched', true)
          } else {
            this.$set(item, 'expand', false)
            this.$set(item, 'matched', false)
          }
          if (item.children && item.children.length > 0) {
            matchData(item.children)
          }
        })
      }
      matchData(this.productData)
      // 子节点展开时，对应的父节点也展开
      const expandData = (data) => {
        data.forEach(x => {
          if (x.children && x.children.length > 0) {
            expandData(x.children)
          }
          if (x.children && x.children.length > 0) {
            const hasExpand = x.children.some(y => y.expand)
            if (hasExpand) {
              this.$set(x, 'expand', true)
            }
          }
        })
      }
      expandData(this.productData)
      // 在该处实现数据过滤：1.保留 matched === true 的节点 2.保留其所有祖先节点 3.matched 节点的所有子孙节点全部保留展示
      const filterWithRelations = (nodes) => {
        if (!Array.isArray(nodes)) return []
        const result = []
        nodes.forEach(node => {
          const hasChildren = Array.isArray(node.children) && node.children.length > 0
          if (node.matched) {
            // 命中节点：自身与其所有后代全部展示
            const kept = this.deepCloneNode(node)
            kept.expand = true
            kept.children = hasChildren ? node.children : []
            result.push(kept)
          } else {
            // 未命中：若后代存在需要展示的节点，则保留当前节点并过滤其子节点
            const filteredChildren = hasChildren ? filterWithRelations(node.children) : []
            if (filteredChildren.length > 0) {
              const kept = this.deepCloneNode(node)
              kept.expand = true
              kept.children = filteredChildren
              result.push(kept)
            }
          }
        })
        return result
      }
      this.filterProductData = filterWithRelations(this.productData)
      this.updateCurrentTreeData()
    },
    /**
     * 深拷贝节点数据，保持引用关系
     * @param {Object} node - 要拷贝的节点对象
     * @returns {Object} 拷贝后的节点对象
     */
    deepCloneNode(node) {
      const cloned = { ...node }
      // 保持原有的勾选状态
      if (Object.prototype.hasOwnProperty.call(node, 'checked')) {
        cloned.checked = node.checked
      }
      if (Object.prototype.hasOwnProperty.call(node, 'indeterminate')) {
        cloned.indeterminate = node.indeterminate
      }
      return cloned
    },
    /**
     * 渲染树节点内容
     * @param {Function} h - Vue 渲染函数
     * @param {Object} param1 - 包含节点数据的对象
     * @returns {VNode} 渲染后的虚拟节点
     */
    renderTreeContent(h, { data }) {
      return h(
        'span',
        {},
        this.highlightMatch(data.title, this.searchParams.displayName, data.matched, h)
      )
    },
    /**
     * 高亮匹配的关键词
     * @param {string} title - 节点标题
     * @param {string} keyword - 搜索关键词
     * @param {boolean} matched - 是否匹配
     * @param {Function} h - Vue 渲染函数
     * @returns {Array|string} 高亮后的内容
     */
    highlightMatch(title, keyword, matched, h) {
      if (!keyword) return title   
      const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') // 转义正则特殊字符
      const regex = new RegExp(`(${escapedKeyword})`, 'gi') // 全局不区分大小写匹配
      
      return title.split(regex).map((part, index) => {
        if (index % 2 === 0) {
          // 非匹配部分
          return part
        } else {
          // 匹配部分，添加高亮样式
          return h(
            'span',
            { style: {'color': matched ? '#5384ff' : '', fontWeight: 'bold'} },
            part
          )
        }
      })
    },
    /**
     * 处理产品选择变化
     * 同步勾选状态并触发选择变化事件
     */
    handleProductSelect() {
      // 如果当前使用过滤后的数据，需要同步状态到原始数据
      if (this.searchParams.displayName) {
        this.syncCheckState(this.filterProductData, this.productData)
      }
      this.selectionList = this.getCheckedLevel2Nodes(this.productData)
      this.$emit('checkChange', this.selectionList, this.productData)
    },
    /**
     * 同步勾选状态
     * 将过滤数据中的勾选状态同步到原始数据中
     * @param {Array} sourceData - 源数据（过滤后的数据）
     * @param {Array} targetData - 目标数据（原始数据）
     */
    syncCheckState(sourceData, targetData) {
      if (!Array.isArray(sourceData) || !Array.isArray(targetData)) return   
      sourceData.forEach(sourceNode => {
        // 在 targetData 中找到对应的节点
        const targetNode = this.findNodeById(targetData, sourceNode.id)
        if (targetNode) {
          // 同步勾选状态
          this.$set(targetNode, 'checked', sourceNode.checked)
          if (Object.prototype.hasOwnProperty.call(sourceNode, 'indeterminate')) {
            this.$set(targetNode, 'indeterminate', sourceNode.indeterminate)
          }
          // 递归同步子节点
          if (sourceNode.children && targetNode.children) {
            this.syncCheckState(sourceNode.children, targetNode.children)
          }
        }
      })
    },
    /**
     * 根据ID查找节点
     * @param {Array} nodes - 节点数组
     * @param {string|number} id - 节点ID
     * @returns {Object|null} 找到的节点或null
     */
    findNodeById(nodes, id) {
      if (!Array.isArray(nodes)) return null
      for (const node of nodes) {
        if (node.id === id) {
          return node
        }
        if (node.children) {
          const found = this.findNodeById(node.children, id)
          if (found) return found
        }
      }
      return null
    },
    /**
     * 获取勾选的二级产品节点
     * @param {Array} nodes - 节点数组
     * @returns {Array} 勾选的二级节点列表
     */
    getCheckedLevel2Nodes(nodes) {
      if (!Array.isArray(nodes)) return []     
      const checkedNodes = []       
      const traverse = (nodeList) => {
        nodeList.forEach(node => {
          if (node.level === 2 && node.checked) {
            checkedNodes.push(node)
          }
          if (node.children && node.children.length > 0) {
            traverse(node.children)
          }
        })
      }   
      traverse(nodes)
      return checkedNodes
    }
  }
}
</script>
<style lang="scss" scoped>
</style>
