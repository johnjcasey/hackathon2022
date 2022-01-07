package sampletransform;

import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollection;

public class CrossLanguageTransform extends PTransform<PCollection<String>, PCollection<String>> {

  private final Integer maxLength;
  private final Integer minLength;

  public CrossLanguageTransform(LengthConfig lengthConfig){
    this.maxLength = lengthConfig.maxLength;
    this.minLength = lengthConfig.minLength;
  }

  public CrossLanguageTransform withOtherConfig(OtherConfig otherConfig){
    return this;
  }

  @Override
  public PCollection<String> expand(PCollection<String> input) {
    return input.apply(Filter.by(checkLength()));
  }

  private SerializableFunction<String,Boolean> checkLength(){
    return input ->
        input.length() <= maxLength && (null == minLength || input.length() >= minLength);
  }

  public static class LengthConfig{
    public int maxLength;
    public int minLength;
  }

  public static class NestedConfig{
    public String string;
  }

  public static class OtherConfig{
    public int k;
    public NestedConfig nestedConfig;
  }

}
