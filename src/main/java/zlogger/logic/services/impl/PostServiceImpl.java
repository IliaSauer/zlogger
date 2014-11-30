package zlogger.logic.services.impl;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zlogger.logic.dao.PostDao;
import zlogger.logic.models.Post;
import zlogger.logic.models.User;
import zlogger.logic.models.Wall;
import zlogger.logic.services.PostService;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostDao postDao;

    @Override
    @Transactional
    public List<Post> list() {
        return postDao.getPosts();
    }

    @Override
    @Transactional
    public List<Post> listForWall(Wall wall) {
        Objects.requireNonNull(wall, "Can't get posts for null wall");
        Objects.requireNonNull(wall.getWallId(),
                "Can't get posts for wall with null wall_id");

        return postDao.getPostsByWall(wall);
    }

    @Override
    @Transactional
    public List<Post> listForUser(User user) {
        Objects.requireNonNull(user, "Can't get posts for null user");
        Objects.requireNonNull(user.getUsername(),
                "Can't get posts for user with null username");

        return postDao.getPostsByUser(user);
    }

    @Override
    @Transactional
    public Long add(Post post, Wall wall, User user) {
        post.setWall(wall);
        post.setCreator(user);
        return add(post);
    }

    @Override
    @Transactional
    public Long add(Post entity) {
        Objects.requireNonNull(entity, "Can't create null post");
        Wall wall = entity.getWall();
        User user = entity.getCreator();
        Objects.requireNonNull(wall, "Can't create post with null wall");
        Objects.requireNonNull(user, "Can't create post with null user");
        Objects.requireNonNull(wall.getWallId(),
                "Can't create post with null wall_id");
        Objects.requireNonNull(user.getUsername(),
                "Can't create post with null creator_id");
        if (!user.getEnabled()) {
            throw new IllegalStateException("Can't add post with disabled creator");
        }

        entity.setCreationDate(new Date());
        try {
            return postDao.createPost(entity);
        } catch (ConstraintViolationException e) {
            Logger logger = Logger.getGlobal();
            logger.log(Level.WARNING, "ConstraintViolationException: " + e);
            throw new IllegalArgumentException("Post is malformed or it's " +
                    "dependencies are not persistent. " +
                    "Violated constraint: " + e.getConstraintName()
            );
        }
    }

    @Override
    @Transactional
    public Post get(Long id) {
        Objects.requireNonNull(id, "Can't get post with null id");

        return postDao.getPostById(id);
    }

    @Override
    @Transactional
    public Long update(Post post) {
        Objects.requireNonNull(post, "Can't update with null post");
        Objects.requireNonNull(post.getId(), "Can't update post with null id");

        Post oldPost = get(post.getId());

        if (post.getMessage() == null) {
            post.setMessage(oldPost.getMessage());
        }
        if (post.getTitle() == null) {
            post.setTitle(oldPost.getTitle());
        }
        post.setCreationDate(oldPost.getCreationDate());
        post.setWall(oldPost.getWall());
        post.setCreator(oldPost.getCreator());
        return postDao.updatePost(post);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Objects.requireNonNull(id, "Can't delete post with null id");

        postDao.deletePostById(id);
    }

}
