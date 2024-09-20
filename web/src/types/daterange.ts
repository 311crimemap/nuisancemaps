export type FilterDateDispatcher = (
    action: { type: string; value: any }
) => void;

export interface DateRange {
  date: {
    startDate: string;
    endDate: string;
  };
  isBusy: boolean;
}

export interface InputDateRange {
    startDate: {
        date: string;
        isValid: boolean;
    };
    endDate: {
        date: string;
        isValid: boolean;
    };
}

// T - 1
const endDate = new Date();
endDate.setDate(endDate.getDate() - 1);

// T - 3 months
// nyc publishes quarterly - want some data available on default
// request
const startDate = new Date();
startDate.setMonth(endDate.getMonth() - 3)


export const defaultDateRange: DateRange = {
    date: {
        startDate: startDate.toLocaleDateString("en-CA"),
        endDate: endDate.toLocaleDateString("en-CA"),
    },
    isBusy: false,
};
