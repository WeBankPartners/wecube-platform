<template>
  <div>
    <Col span="3" v-for="(menuGroup, index) in menus" :key="menuGroup.category">
      <List split :header="menuGroup.category" size="small">
        <ListItem v-for="(menu, index) in menuGroup.children" :key="index">
          <Badge
            :status="menu.menuType === 'plugin' ? 'success' : 'default'"
            :text="menu.displayName"
          />
        </ListItem>
      </List>
    </Col>
  </div>
</template>

<script>
import { getMenuInjection } from "@/api/server";
export default {
  name: "menu-injection",
  data() {
    return {
      menus: []
    };
  },
  watch: {
    pkgId: {
      handler: () => {
        this.getData();
      }
    }
  },
  props: {
    pkgId: {
      required: true,
      type: Number
    }
  },
  created() {
    this.getData();
  },
  methods: {
    async getData() {
      // let { status, data, message } = await getMenuInjection(this.pkgId);
      let { status, data, message } = await getMenuInjection(4);
      if (status === "OK") {
        let allCats = data.map(_ => _.category);
        let cats = Array.from(new Set(allCats));
        this.menus = cats.map(_ => {
          let children = [];
          data.forEach(item => {
            if (item.category === _) {
              children.push(item);
            }
          });
          return {
            category: _,
            children: children
          };
        });
      }
    }
  }
};
</script>
