import gun from "../../assets/gun.svg";
import mask from "../../assets/mask.svg";
import robbery from "../../assets/robbery.svg";
import bolt from "../../assets/bolt-solid.svg";
import dog from "../../assets/dog-solid.svg";
import landmark from "../../assets/landmark-solid.svg";
import window from "../../assets/person-through-window-solid.svg";
import tree from "../../assets/tree-solid.svg";
import bullhorn from "../../assets/bullhorn-solid.svg";
import angry from "../../assets/face-angry-regular.svg";
import hand from "../../assets/hand-fist-solid.svg";
import sack from "../../assets/sack-dollar-solid.svg";
import wrench from "../../assets/wrench-solid.svg";
import car from "../../assets/car-side-solid.svg";
import file from "../../assets/file-contract-solid.svg";
import house from "../../assets/house-circle-exclamation-solid.svg";
import recycle from "../../assets/recycle-solid.svg";
import scale from "../../assets/scale-balanced-solid.svg";
import question from "../../assets/circle-question-regular.svg";
import gavel from "../../assets/gavel-solid.svg";
import houseflood from "../../assets/house-flood-water-solid.svg";
import barrier from "../../assets/road-barrier-solid.svg";
import spray from "../../assets/spray-can-solid.svg";
import credit from "../../assets/credit-card-solid.svg";
import medical from "../../assets/kit-medical-solid.svg";
import burst from "../../assets/person-burst-solid.svg";
import road from "../../assets/road-solid.svg";
import traffic from "../../assets/traffic-light-solid.svg";

export default class AssetLoader {

    //WORKING HERE:
    //1. move category - icon into api?
    //  imports done via returned url field in api?
    // category: {icon: {format: svg, name, url} }
    // keeps it all in one place, don't need to do mapping code.
    // have to do Icon, migration, IconDTO
    //

    static iconsCrime = {
        window, //property
        mask,//theft (alt sack)
        credit,//fraud
        gun,//vice, weapon
        gavel,//public order
        hand,  //violent
    }

    static icons311 = {
        burst, //quality of life
        dog, //animal
        bullhorn, //noise
        spray, //vandalism
        scale, //regulatory
        house, //code compliance
        angry, //consumer comnplaint
        wrench, //infra
        road, //road
        traffic,
        houseflood, //storm drainage
        barrier, //street hazard
        car, //mobility
        landmark, //municipal
        tree, //parks, env
        recycle, //waste
        medical, //public health
        question, //other
        bolt, //telecom
    }

    //NB: sdf is optimziation for monochromatic png; esp for paint on top.
    static async load(map) {

        let icons = {
            ...this.iconsCrime,
            ...this.icons311,

            robbery, //for existing code
            //file,
        }

        const images = Object.entries(icons).map(([key, val]) => {
            return { format: 'svg', name: key, url: val }
        });


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

        const keywords = [
            {
                keyword: "theft",
                //filename / dim?
                icon: "robbery",
            },
            {
                keyword: "assault",
                icon: "gun",
            },
            {
                keyword: "criminal",
                icon: "mask",
            },
        ];

        if (!data.features.length) return;

        for (const feature of data.features) {

            for (const ki of keywords) {
                if (feature.properties['reportCategory'].toLowerCase().includes(ki.keyword)) {
                    feature.properties['icon-category'] = ki.icon;
                    break;
                }
            }
            //default
            if (!feature.properties['icon-category']) {
                feature.properties['icon-category'] = "cluster"
            }
        }
    }

    static setIconCategory311(data: any) {
        for (const feature of data.features) {

            //default
            if (!feature.properties['icon-category']) {
                feature.properties['icon-category'] = "cluster-sdf"
            }
        }
    }

}
