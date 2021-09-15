process.env.VUE_APP_PRODUCT   = process.env.PRODUCT || process.env.ontology_publisher_current_product || 'htmlpages'
process.env.VUE_APP_BRANCH    = (process.env.BRANCH || ( process.env.BRANCH_NAME === process.env.TAG_NAME ? 'master' : process.env.BRANCH_NAME || 'master')).toLowerCase()
process.env.VUE_APP_TAG       = process.env.TAG || process.env.TAG_NAME || 'latest'
process.env.VUE_APP_TIMESTAMP = process.env.TIMESTAMP || '2021Q2'

module.exports = {
    publicPath: `/${process.env.ONTPUB_FAMILY || 'fibo'}/`,
    assetsDir: `${process.env.VUE_APP_PRODUCT}/${process.env.VUE_APP_BRANCH}/${process.env.VUE_APP_TAG}`,
    indexPath: `${process.env.VUE_APP_PRODUCT}/${process.env.VUE_APP_BRANCH}/${process.env.VUE_APP_TAG}/index.html`,
    css: {
        loaderOptions: {
            sass: {
                data: '@import "@/styles/global.scss";',
            },
        },
    },
    runtimeCompiler: true,
    devServer: {
        proxy: {
            '^/fibo/ontology/.*api/search': {
                target: 'http://onto-viewer-server:8080',
            },
            '^/fibo/ontology/.*api/module$': {
                target: 'http://onto-viewer-server:8080',
            },
            '^/fibo/ontology/.*api/hint': {
                target: 'http://onto-viewer-server:8080',
            },
            '^/fibo/ontology/api': {
                target: 'http://onto-viewer-server:8080',
            },
        },
    },
};
