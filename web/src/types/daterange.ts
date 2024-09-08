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
