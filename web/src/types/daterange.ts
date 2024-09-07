export type FilterDateDispatcher = (
  filterDate: DateRange,
  action: { type: string; value: any }
) => DateRange;

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
