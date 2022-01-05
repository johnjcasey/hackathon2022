package sampletransform;

import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollection;

public class CrossLanguageTransform extends PTransform<PCollection<String>, PCollection<String>> {

  private final Integer maxLength;
  private Integer minLength;

  public CrossLanguageTransform(int maxLength){
    this.maxLength = maxLength;
  }

  public CrossLanguageTransform withMinLength(Integer minLength){
    this.minLength = minLength;
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

}
