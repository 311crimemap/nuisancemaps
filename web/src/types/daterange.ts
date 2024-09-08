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


const endDate = new Date();
endDate.setDate(endDate.getDate() - 1);

export const defaultDateRange: DateRange = {
    date: {
        startDate: new Date("01-01-2024").toLocaleDateString("en-CA"),
        endDate: endDate.toLocaleDateString("en-CA"),
    },
    isBusy: false,
};
