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

      if (value == "month") {
        calcDate.setMonth(calcDate.getMonth() - 1);
        return {
          date: {
            endDate: endMaxDate,
            startDate: calcDate.toLocaleDateString("en-CA"),
          },
        };
      }

      if (!isNaN(Number(value))) {
        calcDate.setDate(calcDate.getDate() - Number(value));
        return {
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
        date: {
          ...filterDate.date,
          ...action.date,
        },
      };
    }
    default: {
      throw new Error("action doesn't exist", action);
    }
  }
}
