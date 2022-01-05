import argparse

import apache_beam as beam
from apache_beam.options.pipeline_options import PipelineOptions
from apache_beam.testing.util import assert_that, equal_to
from apache_beam.transforms.external import JavaExternalTransform
import numpy


def run(argv=None):
  parser = argparse.ArgumentParser()

  known_args, pipeline_args = parser.parse_known_args(argv)
  pipeline_options = PipelineOptions(pipeline_args)

  java_transform = (JavaExternalTransform('sampletransform.CrossLanguageTransform', expansion_service='localhost:9090')
                    (numpy.int32(7))).withMinLength(numpy.int32(4))

  with beam.Pipeline(options=pipeline_options) as p:
    res = (
        p
        | 'Create' >> beam.Create(['srt', 'medium', 'longword'])
        | 'Filter' >> java_transform)
    assert_that(res, equal_to(['srt', 'medium']))


if __name__ == '__main__':
  run()

