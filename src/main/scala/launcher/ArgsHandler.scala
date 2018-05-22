package launcher

object ArgsHandler {
  type Args = Array[String]
  type Options = Map[String, String]

  val defaults: Map[String, String] = Map(
    "data-path" -> "data/",
    "samples" -> "0",
    "mode" -> "async",
    "port" -> "50500",
    "interval" -> "500",
  )

  def argsToMap(args: Args): Options =
    defaults ++ args.flatMap { arg =>
      arg.split("=").toList match {
        case name :: value :: Nil if name != "" && value != "" =>
          List(name.toLowerCase -> value.toLowerCase)
        case _ => Nil
      }
    }.toMap
}