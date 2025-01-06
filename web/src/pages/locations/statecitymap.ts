import { CategoryMinMaxReportedAt } from "../../types/categoryminmaxreportedat";

export interface StateCity {
    name: string;
    city: string;
    state: string;
    categoryMinMaxReportedAt: CategoryMinMaxReportedAt[] | null;
}

export interface StateCityMap {
  [state: string]: StateCity[];
}
