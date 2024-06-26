export default function dateFilterReducer(filterDate, action) {
  //console.log("INIT", filterDate);
  // {
  //     type: "init",
  //     date: {
  //         startDate, endDate
  //     }
  // }
  switch (action.type) {
    case "calcDate": {
      const calcDate = new Date();
      const max = new Date();
      const endMaxDate = max.toLocaleDateString("en-CA");
      const value = action.value;

      if (value.includes("month")) {
        const months = value.split(" ")[0];
        calcDate.setMonth(calcDate.getMonth() - months);
        return {
          ...filterDate,
          date: {
            endDate: endMaxDate,
            startDate: calcDate.toLocaleDateString("en-CA"),
          },
        };
      }

      if (!isNaN(Number(value))) {
        calcDate.setDate(calcDate.getDate() - Number(value));
        return {
          ...filterDate,
          date: {
            endDate: endMaxDate,
            startDate: calcDate.toLocaleDateString("en-CA"),
          },
        };
      }
      break;
    }
    case "setDate": {
      return {
        ...filterDate,
        date: {
          ...filterDate.date,
          ...action.date,
        },
      };
    }
    case "isBusy": {
      return {
        ...filterDate,
        isBusy: action.value,
      };
    }
    default: {
      throw new Error("action doesn't exist", action);
    }
  }
}
