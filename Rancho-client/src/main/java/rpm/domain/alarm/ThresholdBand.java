package rpm.domain.alarm;

public final class ThresholdBand {

    public final double lowAmber;
    public final double lowRed;
    public final double highAmber;
    public final double highRed;

    /* Reference [1] - taken from OpenAI ChatGPT (https://chat.openai.com)
       Design suggestion: unused threshold limits can be set to +/- infinity
       when only one side of the range is required.
    */
    public ThresholdBand(double lowAmber, double lowRed,
                         double highAmber, double highRed) {
        this.lowAmber = lowAmber;
        this.lowRed = lowRed;
        this.highAmber = highAmber;
        this.highRed = highRed;
    }
    /* end of reference 1 */
}
