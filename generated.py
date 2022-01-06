from apache_beam.transforms.external import JavaExternalTransform
from apache_beam.transforms import PTransform
import numpy
import typing


class CrossLanguageTransform(PTransform):

  def __init__(self,
      expansion_service,
      length_config,
      # Java Type class sampletransform.CrossLanguageTransform$LengthConfig
      min_length=None  # Java Type class java.lang.Integer
  ):
    super().__init__()
    self.expansion_service = expansion_service
    self.length_config = length_config
    self.min_length = min_length

  def generate_java_transform(self):
    base_transform = (
      JavaExternalTransform('sampletransform.CrossLanguageTransform',
                            expansion_service=self.expansion_service))
    constructed_transform = base_transform(
      self.length_config
    )
    if self.min_length is not None:
      constructed_transform = constructed_transform.withMinLength(
        numpy.int64(self.min_length))

    return constructed_transform

  def expand(self, pcoll):
    java_transform = self.generate_java_transform()
    return (
        pcoll
        | 'CrossLanguageTransform' >> java_transform)


LengthConfig = typing.NamedTuple(
  'LengthConfig', [
    ('maxLength', int64),
    ('minLength', int64)
  ])
