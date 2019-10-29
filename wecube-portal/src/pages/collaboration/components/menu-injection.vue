<template>
  <div>
    <Col span="4" v-for="(menuGroup, index) in menus" :key="menuGroup.id">
      <List size="small">
        <h4 slot="header">{{ menuGroup.displayName }}</h4>
        <ListItem
          v-for="(menu, index) in menuGroup.children"
          :key="index"
          style="padding-right: 10px"
        >
          <Tooltip
            :content="menu.displayName"
            placement="bottom"
            style="width: 100%"
          >
            <p
              v-if="menu.menuType === 'package'"
              style="color: green;width: 100%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;"
            >
              {{ menu.displayName }}
            </p>
            <p
              v-else
              style="width: 100%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;"
            >
              {{ menu.displayName }}
            </p>
          </Tooltip>
        </ListItem>
      </List>
    </Col>
  </div>
</template>

<script>
import { getMenuInjection } from "@/api/server";
import { MENUS } from "../../../const/menus.js";

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
      let { status, data, message } = await getMenuInjection(this.pkgId);
      if (status === "OK") {
        let allCats = [];
        data.forEach((_, index) => {
          if (!_.category && _.code !== "COLLABORATION" && _.code !== "ADMIN") {
            const found = MENUS.find(m => m.code === _.code);
            allCats.push({
              id: _.id,
              code: _.code,
              displayName: this.$lang === "zh-CN" ? found.cnName : found.enName,
              children: []
            });
          }
        });

        this.menus = allCats.map(_ => {
          data.forEach(item => {
            if (item.category === "" + _.id) {
              if (item.menuType === "system") {
                const found = MENUS.find(m => m.code === item.code);
                if (found) {
                  item.displayName =
                    this.$lang === "zh-CN" ? found.cnName : found.enName;
                }
              }
              _.children.push(item);
            }
          });
          return _;
        });
      }
    }
  }
};
</script>
