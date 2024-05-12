import robbery from "../../assets/robbery.svg";

const ASSET_PATH = "../../assets";

export default class AssetLoader {


    //NB: sdf is optimziation for monochromatic png; esp for paint on top.
    static async load(map, categories) {

        const filtered_categories = categories.filter((cat, i, self) => {
            return i == self.findIndex(c => cat.iconName == c.iconName);
        })

        const images = [];
        for (const category of filtered_categories) {
            if (category.iconFilename && category.iconFilename.endsWith("svg")) {
                const url = new URL(`${ASSET_PATH}/${category.iconFilename}`, import.meta.url).href;
                images.push({ format: "svg", name: category.iconName, url });
            }
        }


        //TODO: used in examples, but eventually needs to be removed
        images.push({ format: "svg", name: "robbery", url: robbery });

        console.log("IMAGES", images);
        try {

            const imgs = [
                { format: 'png', name: 'cluster', url: 'https://raw.githubusercontent.com/nazka/map-gl-js-spiderfy/dev/demo/img/circle-yellow.png' },
                { format: 'png', name: 'cluster-sdf', url: 'https://raw.githubusercontent.com/nazka/map-gl-js-spiderfy/dev/demo/img/circle-sdf.png' },
            ].concat(images);


            for (const image of imgs) {

                if (image.format == "svg") {
                    let img = new Image(20, 20)
                    img.onload = () => map.addImage(image.name, img)
                    img.src = image.url;
                } else {
                    const { data } = await map.loadImage(image.url);
                    map.addImage(image.name, data);
                }
            }

        } catch (e) {
            console.log("[AssetLoader] ERR", e);
        }


    }

    static setIconCategory(data: any) {

        if (!data.features.length) return;

        for (const feature of data.features) {

            if (feature.properties['category']['iconName']) {
                feature.properties['icon-category'] = feature.properties['category']['iconName']
            }

        }
    }

}
