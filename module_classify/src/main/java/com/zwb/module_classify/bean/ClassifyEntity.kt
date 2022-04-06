package com.zwb.module_classify.bean

data class ClassifyEntity(var goodsClassID:Int,
                          var parentID:Int,
                          var goodsClassName:String,
                          var picURL:String?,
                          var children:List<ClassifyEntity>?,
                          var isSelected:Boolean)