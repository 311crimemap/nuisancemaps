class Logger {
  readonly log: (...args: any[]) => void;
  readonly warn: (...args: any[]) => void;
  readonly error: (...args: any[]) => void;

  sessionId;

  constructor() {
    this.log = () => {};
    this.warn = () => {};
    this.error = () => {};

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get("debug") === "true") {
      this.log = console.log.bind(console);
      this.warn = console.warn.bind(console);
      this.error = console.error.bind(console);

      this.sessionId = crypto.randomUUID();
    }
  }

  get data() {
    const err = new Error();
    const stack = err.stack || "";
    const stackLines = stack.split("\n");
    let componentName;

    try {
      if (stackLines.length >= 1) {
        // first entry is this getter
        componentName = stackLines[1].split("/")[0].split("@")[0];
      }
    } catch (e) {
      componentName = undefined;
    }

    return {
      componentName,
      sessionId: this.sessionId,
      timestamp: Date.now(),
    };
  }
}

export const Log = new Logger();
