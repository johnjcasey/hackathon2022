class CrossLanguageTransform(PTransform):

    def __init__(self,
                expansion_service,
                            maxLength #Java Type int
                                            ,
                                minLength = None #Java Type class java.lang.Integer
                                            ):
      self.expansion_service = expansion_service
            self.maxLength = maxLength
                  self.minLength = minLength
        
  def generate_java_transform(self):
    base_transform = (JavaExternalTransform('sampletransform.CrossLanguageTransform', expansion_service=self.expansion_service))
    constructed_transform = base_transform(
            self.maxLength        ).withMinLength(self.minLength)
  def expand(self, pcoll):
    java_transform = generate_java_transform()
    return(
        pcoll
        | 'CrossLanguageTransform' >> java_transform