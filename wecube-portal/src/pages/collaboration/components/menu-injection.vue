<template>
  <div>
    <Col span="4" v-for="menuGroup in menus" :key="menuGroup.id">
      <List size="small">
        <h6 slot="header">{{ menuGroup.displayName }}</h6>
        <ListItem v-for="(menu, index) in menuGroup.children" :key="index" style="padding-right: 10px">
          <Tooltip
            :content="$lang === 'zh-CN' ? menu.localDisplayName : menu.displayName"
            placement="bottom"
            style="width: 100%"
          >
            <p :class="menu.source === 'SYSTEM' ? 'menu-injection_menu-item' : 'menu-injection_menu-item_new'">
              {{ $lang === 'zh-CN' ? menu.localDisplayName : menu.displayName }}
            </p>
          </Tooltip>
        </ListItem>
      </List>
    </Col>
  </div>
</template>

<script>
import { getMenuInjection } from '@/api/server'
import { MENUS } from '../../../const/menus.js'

export default {
  name: 'menu-injection',
  data () {
    return {
      menus: []
    }
  },
  watch: {
    pkgId: {
      handler: () => {
        this.getData()
      }
    }
  },
  props: {
    pkgId: {
      required: true
    }
  },
  created () {
    this.getData()
  },
  methods: {
    async getData () {
      let { status, data } = await getMenuInjection(this.pkgId)
      if (status === 'OK') {
        let allCats = []
        data.forEach((_, index) => {
          if (!_.category && _.code !== 'COLLABORATION' && _.code !== 'ADMIN') {
            const found = MENUS.find(m => m.code === _.code)
            allCats.push({
              id: _.id,
              code: _.code,
              displayName: this.$lang === 'zh-CN' ? found.cnName : found.enName,
              children: []
            })
          }
        })

        this.menus = allCats.map(_ => {
          data.forEach(item => {
            if (item.category === '' + _.id && item.source !== 'SYSTEM') {
              _.children.push(item)
            }
          })
          return _
        })
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.menu-injection_menu-item {
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.menu-injection_menu-item_new {
  width: 100%;
  color: green;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
