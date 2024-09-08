import { Category } from "./category"
import { FeatureCollection } from "./features"

export interface InitSourceCategoryResponse  {
    status: string;
    data: {
        sources: FeatureCollection;
        categories: Category[];
    }
}
