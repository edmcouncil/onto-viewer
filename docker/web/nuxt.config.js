import axios from "axios";

process.env.VUE_APP_TIMESTAMP =
  process.env.TIMESTAMP || process.env.VUE_ONTOLOGY_NAME==='idmp'?"latest":"2022Q3";

process.env.VUE_APP_PRODUCT =
  process.env.PRODUCT ||
  process.env.ontology_publisher_current_product ||
  "pages";
process.env.VUE_APP_BRANCH = (
  process.env.BRANCH ||
  (process.env.BRANCH_NAME === process.env.TAG_NAME
    ? "develop"
    : process.env.BRANCH_NAME || "develop")
).toLowerCase();
process.env.VUE_APP_TAG = process.env.TAG || process.env.TAG_NAME || "latest";

process.env.VUE_DIST_DIR = `/${process.env.VUE_APP_PRODUCT}/${process.env.VUE_APP_BRANCH}/${process.env.VUE_APP_TAG}`;
process.env.VUE_ASSETS_DIR = `${process.env.VUE_DIST_DIR}/_nuxt/`;

process.env.VUE_ONTOLOGY_NAME = process.env.ONTPUB_FAMILY || "iof";
process.env.VUE_BASE_URL =
  process.env.BASE_URL ||
  "https://spec." +
    (process.env.VUE_ONTOLOGY_NAME === "idmp"
      ? "pistoiaalliance"
      : "edmcouncil") +
    ".org/";

process.env.VUE_RESOURCES_BASE_URL = process.env.VUE_BASE_URL + process.env.VUE_ONTOLOGY_NAME + "/ontology/";

process.env.STRAPI_URL = process.env.STRAPI_URL || "http://localhost:1337";

export default {
  // target: 'static' description https://nuxtjs.org/announcements/going-full-static/
  target: "static",
  // Global page headers: https://go.nuxtjs.dev/config-head
  head: {
    title: process.env.VUE_ONTOLOGY_NAME.toUpperCase(),
    htmlAttrs: {
      lang: "en",
    },
    meta: [
      { charset: "utf-8" },
      { name: "viewport", content: "width=device-width, initial-scale=1" },
      { hid: "description", name: "description", content: "" },
      {
        hid: "keywords",
        name: "keywords",
        content:
          "ontology, Open Knowledge Graph, OKG, EDM Council, Enterprise Data Management Council",
      },
      { name: "format-detection", content: "telephone=no" },
    ],
    link: [{ rel: "icon", type: "image/x-icon", href: "/favicon.ico" }],
    script: [
      {
        src: "https://cdn.jsdelivr.net/npm/jquery@3.5.1/dist/jquery.slim.min.js",
        integrity:
          "sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj",
        crossorigin: "anonymous",
      },
      {
        src: "https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js",
        integrity:
          "sha384-9/reFTGAW83EW2RDu2S0VKaIzap3H66lZH81PoYlFhbGU+6BZp6G7niu735Sk7lN",
        crossorigin: "anonymous",
      },
      {
        src: "https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/js/bootstrap.min.js",
        integrity:
          "sha384-VHvPCCyXqtD5DqJeNxl2dtTyhF78xXNXdkwX1CZeRusQfRKp+tA7hAShOK/B/fQ2",
        crossorigin: "anonymous",
      },
    ],
  },
  server: {
    host: process.env.PUBLIC_URL || "localhost",
  },
  router: {
    base: `/${process.env.VUE_ONTOLOGY_NAME}`,
  },

//to removed
  generate: {
    dir: `dist/${process.env.VUE_ONTOLOGY_NAME}${process.env.VUE_DIST_DIR}`,
    routes() {
      return axios
        .get(`${process.env.STRAPI_URL || "http://localhost:1337"}/api/pages`)
        .then((res) => {
          return res.data.data.map((page) => {
            const slug = page.attributes.slug;
            return `/page/${slug}`;
          });
        });
    },
  },

  // loading bar
  loading: {
    color: "black",
    height: "5px",
  },

  // Global CSS: https://go.nuxtjs.dev/config-css
  css: ["./assets/scss/Ontology.scss"],

  // Plugins to run before rendering page: https://go.nuxtjs.dev/config-plugins
  plugins: [
    { src: "~/plugins/vue-multiselect" },
    { src: "~/plugins/v-clipboard" },
  ],

  // Auto import components: https://go.nuxtjs.dev/config-components
  components: {
    dirs: [
      "~/components",
      "~/components/Articles",
      "~/components/chunks",
      "~/components/Ontology",
    ],
  },

  // Modules for dev and build (recommended): https://go.nuxtjs.dev/config-modules
  buildModules: [
    [
      "@nuxtjs/google-fonts",
      {
        families: {
          Inter: {
            wght: [300, 400, 500],
          },
        },
        display: "swap",
        prefetch: false,
        preconnect: false,
        preload: false,
        download: true,
        base64: false,
      },
    ],
  ],

  // Modules: https://go.nuxtjs.dev/config-modules
  modules: [
    "@nuxtjs/style-resources",
    "bootstrap-vue/nuxt",
    "@nuxtjs/markdownit",
    "@nuxtjs/proxy",
  ],

  // Build Configuration: https://go.nuxtjs.dev/config-build
  build: {
    standalone: true,
    publicPath: process.env.VUE_ASSETS_DIR,
    loaders: {
      sass: {
        implementation: require("sass"),
      },
      scss: {
        implementation: require("sass"),
      },
    },
    extend(config) {
      config.resolve.alias.vue = "vue/dist/vue.common";
    },
  },

  env: {
    generateDir: `dist/${process.env.VUE_ONTOLOGY_NAME}`,
    ontologyName: process.env.VUE_ONTOLOGY_NAME,
    assetsDir: process.env.VUE_ASSETS_DIR,
    distDir: process.env.VUE_DIST_DIR,
    staticGenerationMode: process.env.NODE_ENV === "production",
    ontologyResourcesBaseUri: process.env.VUE_RESOURCES_BASE_URL,
    strapiBaseUrl: process.env.STRAPI_URL,
    showTermsLinkOnFooter: process.env.SHOW_TERMS_LINK_ON_FOOTER || true,
  },

  http: {
    proxy: process.env.NODE_ENV !== "production",
  },

  proxy: [
        "http://onto-viewer-server:8080/dev/ontology/api",
        "http://onto-viewer-server:8080/dev/ontology/*/api",
  ],

  styleResources: {
    scss: [
      "~assets/scss/_variables.scss",
      "~assets/scss/_bootstrap-override.scss",
      "~assets/scss/global.scss",
      "~assets/scss/typography.scss",
      "~assets/scss/highlight.scss",
    ],
  },

  markdownit: {
    preset: "default",
    linkify: true,
    breaks: true,
    use: ["markdown-it-div", "markdown-it-attrs", "markdown-it-highlightjs"],
    runtime: true,
  },
};
