import gunSVG from "../../assets/gun.svg";
import maskSVG from "../../assets/mask.svg";
import robberySVG from "../../assets/robbery.svg";

export default class AssetLoader {

    constructor() {}

    //NB: sdf is optimziation for monochromatic png; esp for paint on top.
    async init(map) {

        try {

            for (const image of [
                { format: 'png', name: 'cluster', url: 'https://raw.githubusercontent.com/nazka/map-gl-js-spiderfy/dev/demo/img/circle-yellow.png' },
                { format: 'png', name: 'cluster-sdf', url: 'https://raw.githubusercontent.com/nazka/map-gl-js-spiderfy/dev/demo/img/circle-sdf.png' },
                { format: 'svg', name: 'robbery', url: robberySVG },
                { format: 'svg', name: 'mask', url: maskSVG },
                { format: 'svg', name: 'gun', url: gunSVG }]) {

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
}
