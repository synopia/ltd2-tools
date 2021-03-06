package ltd2

import java.io.PrintStream
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties

fun type2String(type:KType) : String {
    val stype = type.toString().replace("kotlin.","").replace("ltd2.", "")
    if( stype=="DecimalArray" ) {
        return "List<Double>"
    }
    return stype
}

fun value2String(type:KType, value:Any?) : String {
    var stype = type2String(type)
    var optional = false
    if( stype.endsWith("?")) {
        stype = stype.substring(0, stype.length-1)
        optional = true
    }
    if( value==null && optional) {
        return "null"
    }
    when(stype) {
        "String"->return if( value!=null ) "\"$value\"" else ""
        "AttackMode"->return if( value!=null ) "AttackMode.$value" else "AttackMode.Illegal"
        "ArmorType"->return if( value!=null ) "ArmorType.$value" else "ArmorType.Illegal"
        "AttackType"->return if( value!=null ) "AttackType.$value" else "AttackType.Illegal"
        "UnitClass"->return if( value!=null ) "UnitClass.$value" else "UnitClass.Illegal"
        "Legion"->return if( value!=null ) "Legion.$value" else "Legion.Illegal"
        "List<Double>"->return "listOf(${(value as DecimalArray).list.joinToString(",")})"
    }
    return value.toString()
}
fun writeClassDef(cls: KClass<*>) {
    println("data class ${cls.simpleName}(")
    println(cls.memberProperties.map{
        "\tval ${it.name} : ${type2String(it.returnType)}"
    }.joinToString(",\n"))
    println(")")
}
fun <T> writeEnum(name:String, values:Array<T>) {
    println("enum class $name {")
    println(values.map {
        "\t$it"
    }.joinToString(",\n"))
    println("}")
}
fun main(args: Array<String>) {
    val data = loadData("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Legion TD 2")
    (System::setOut)(PrintStream("src/main/kotlin/ltd2.kt"))
    writeEnum("ArmorType", ArmorType.values())
    writeEnum("AttackType", AttackType.values())
    writeEnum("AttackMode", AttackMode.values())
    writeEnum("UnitClass", UnitClass.values())
    writeClassDef(Legion::class)
    writeClassDef(UnitDef::class)
    writeClassDef(WaveDef::class)
    writeClassDef(Global::class)
    println("object LegionData {")
    println("\tval legions = listOf(")
    println(data.legions.legions.map { legion->
        "\t\tLegion(${Legion::class.memberProperties.map{ value2String(it.returnType, it.get(legion))}.joinToString(", ")})"
    }.joinToString(",\n"))
    println("\t)")
    println("\tval legionsMap = legions.associateBy { it.id }\n")
    println("\tval units = listOf(")
    println(data.unitDefs.unitDefs.map {unitDef->
        "\t\tUnitDef(${UnitDef::class.memberProperties.map { value2String(it.returnType, it.get(unitDef)) }.joinToString(", ")})"
    }.joinToString(",\n"))
    println("\t)")
    println("\tval unitsMap = units.associateBy { it.id }\n")
    println("\tval waves = listOf(")
    println(data.waveDefs.waveDefs.sortedBy { it.levelNum }.map { waveDef->
        "\t\tWaveDef(${WaveDef::class.memberProperties.map { value2String(it.returnType, it.get(waveDef)) }.joinToString(", ")})"
    }.joinToString(",\n"))
    println("\t)")
    println("\tval global = Global(${Global::class.memberProperties.map { value2String(it.returnType, it.get(data.global)) }.joinToString(", ")})")
    println("}")
}