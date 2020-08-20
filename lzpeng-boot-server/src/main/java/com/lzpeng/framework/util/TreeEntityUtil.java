package com.lzpeng.framework.util;

import com.lzpeng.framework.domain.TreeEntity;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @date: 2020/4/4
 * @time: 10:41
 * @author:   李志鹏
 */
public class TreeEntityUtil {

    /**
     * 将每个实体的父节点也加入集合中去
     * @param entities
     * @return
     */
    private static <Entity extends TreeEntity<Entity>> List<Entity> addParentToList(Collection<Entity> entities) {
        // 因为是树形结构, 所以将每个实体的父节点也加入返回结果中
        List<Entity> result = new ArrayList<>();
        Queue<Entity> queue = new ConcurrentLinkedQueue<>(entities);
        while (!queue.isEmpty()) {
            //出队
            Entity child = queue.remove();
            if (child.getParent() != null) {
                //入队
                queue.offer(child.getParent());
            }
            // 避免重复元素
            if (!result.contains(child)) {
                result.add(child);
            }
        }
        return result;
    }

    /**
     * 设置子节点为空
     *
     * @param entities
     * @return
     */
    private static <Entity extends TreeEntity<Entity>> List<Entity> setChildrenWithEmpty(List<Entity> entities) {
        if (!CollectionUtils.isEmpty(entities)) {
            for (Entity entity : entities) {
                entity.getChildren().clear();
            }
        }
        return entities;
    }

    /**
     * TODO 将扁平化数据转为树形数据
     * 类似最小生成树算法,通过节点列表，和节点之间的关系将扁平化数据生成树形数据,返回树根集合
     *
     * @param entities
     * @return
     */
    private static <Entity extends TreeEntity<Entity>> List<Entity> convertFlatToTree(List<Entity> entities) {
        List<Entity> result = new ArrayList<>();
        // 收集所有实体 id
        if (!CollectionUtils.isEmpty(entities)) {
            for (Entity entity : entities) {
                // 得到父节点
                Entity parent = entity.getParent();
                // 没有父节点说明是根节点 加入返回集合
                if (parent == null) {
                    result.add(entity);
                } else {
                    // 有父节点则将此节点设置子为其父节点的子节点
                    parent.getChildren().add(entity);
                }
            }
        }
        return result;
    }

    /**
     * 根据顺序号升序排列
     * 设置父节点ID
     * @param entities
     */
    public static <Entity extends TreeEntity<Entity>> void sortTreeData(List<Entity> entities) {
        // 根据顺序号排序
        Queue<Entity> queue = new ConcurrentLinkedQueue<>(entities);
        while (!queue.isEmpty()) {
            // 出队
            Entity parent = queue.remove();
            // 根据顺序号升序排列子节点
            parent.getChildren().sort(Comparator.comparingInt(TreeEntity::getOrderNum));
            for (Entity child : parent.getChildren()) {
                // 设置父节点ID
                child.setParentId(parent.getId());
                // 入队
                queue.offer(child);
            }
        }
        // 根据顺序号升序排列根节点
        entities.sort(Comparator.comparingInt(TreeEntity::getOrderNum));
    }

    private static <Entity extends TreeEntity<Entity>> boolean check(Entity parent, Entity child) {
        if (child.getParent() == null) {
            return false;
        }
        String parentId = parent.getId();
        String childParentId = child.getParent().getId();
        return childParentId.equals(parentId);
    }

    /**
     * 构建树形数据
     * @param entities 从数据库中获取的集合
     * @param <Entity>
     * @return 树形结构数据
     */
    public static <Entity extends TreeEntity<Entity>> List<Entity> treeData(List<Entity> entities){
        // 先构建扁平化数据
        List<Entity> result = flatData(entities);
        // 构建树形结构数据
        result = convertFlatToTree(result);
        // 排序
        sortTreeData(result);
        return result;
    }

    /**
     * 将通过模糊查询获取的集合的父节点加入结果构建扁平化数据
     * @param entities 通过findAll(Entity)从数据库中模糊查询获取的集合
     * @param <Entity>
     * @return 扁平化结构数据
     */
    public static <Entity extends TreeEntity<Entity>> List<Entity> flatData(Collection<Entity> entities){
        // 将每个节点的父节点也加入集合中去
        List<Entity> result = addParentToList(entities);
        // 并删除所有的Children
        result = setChildrenWithEmpty(result);
        // 排序
        result.sort(Comparator.comparingInt(TreeEntity::getOrderNum));
        // 设置父节点ID
        for (Entity entity : result) {
            Entity parent = entity.getParent();
            if (parent != null) {
                entity.setParentId(parent.getId());
            }
        }
        return result;
    }

    /**
     * 将通过findById(String)从数据库中获取的树形结构数据的子节点加入结果构建扁平化数据
     * @param root 通过findById(String)从数据库中获取的树形结构数据
     * @param <Entity>
     * @return 扁平化结构数据
     */
    public static <Entity extends TreeEntity<Entity>> List<Entity> flatData(Entity root){
        // 将每个节点的父节点也加入集合中去
        List<Entity> result = addChildToList(root);
        // 并删除所有的Children
        result = setChildrenWithEmpty(result);
        // 排序
        result.sort(Comparator.comparingInt(TreeEntity::getOrderNum));
        // 设置父节点ID
        for (Entity entity : result) {
            Entity parent = entity.getParent();
            if (parent != null) {
                entity.setParentId(parent.getId());
            }
        }
        return result;
    }

    /**
     * 将根节点的所有字节点加入集合
     * @param root 根节点
     * @param <Entity>
     * @return
     */
    private static <Entity extends TreeEntity<Entity>> List<Entity> addChildToList(Entity root) {

        // 因为是树形结构, 所以将每个实体的子节点也加入返回结果中 BFS 遍历
        List<Entity> result = new ArrayList<>();
        Queue<Entity> queue = new ConcurrentLinkedQueue<>();
        // 入队
        queue.offer(root);
        while (!queue.isEmpty()) {
            // 出队
            Entity parent = queue.remove();
            for (Entity child : parent.getChildren()) {
                // 入队
                queue.offer(child);
                result.add(child);
            }
        }
        return result;
    }
}
