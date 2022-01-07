from apache_beam.transforms.external import JavaExternalTransform
from apache_beam.transforms import PTransform
import numpy
import typing

class CrossLanguageTransform(PTransform):

  def __init__(self,
               expansion_service,
               length_config,  #Java Type class sampletransform.CrossLanguageTransform$LengthConfig
              ):
    super().__init__()
    self.expansion_service = expansion_service
    self.length_config = length_config

  def generate_java_transform(self):
    base_transform = (JavaExternalTransform('sampletransform.CrossLanguageTransform', expansion_service=self.expansion_service))
    constructed_transform = base_transform(
    self.length_config
    )
    if (
        self.other_config is not None
        ):
        constructed_transform = constructed_transform.withOtherConfig(
            self.other_config
        )

    return constructed_transform

  def expand(self, pcoll):
    java_transform = self.generate_java_transform()
    return(
        pcoll
        | 'CrossLanguageTransform' >> java_transform)

OtherConfig = typing.NamedTuple(
'OtherConfig', [
('k', numpy.int32), 
('nestedConfig', NestedConfig)
])

NestedConfig = typing.NamedTuple(
'NestedConfig', [
('string', str)
])

LengthConfig = typing.NamedTuple(
'LengthConfig', [
('maxLength', numpy.int32), 
('minLength', numpy.int32)
])

